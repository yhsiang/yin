;; The Y Programming Language (Version 1)

#lang typed/racket

(require/typed racket
               [string-split (String (U String Regexp) -> (Listof String))])

(require racket/pretty)

(provide view test)


;; --------------- utilities ---------------
(define (abort who . args)
  (printf "~s: " who)
  (for-each display args)
  (display "\n")
  (error 'infer ""))


;; probs for debugging purposes
(define *debug* #t)
(define-syntax peek
  (syntax-rules ()
    [(_ args ...)
     (if *debug*
         (begin
           (printf "~s = ~s~n" 'args args)
           ...)
         (void))]))


(define-syntax test
  (syntax-rules ()
    [(_ name expected exp)
     (begin
       (printf "testing: ~a ... " name)
       (let ([result (view exp)])
         (cond
          [(equal? result expected)
           (printf "~n[pass]~n")]
          [else
           (printf "~n[fail]!~n")
           (printf "- [expected]~n")
           (pretty-print expected)
           (printf "- [actual]~n")
           (pretty-print result)
           (printf "- [input] ~n")
           (pretty-print exp)])))]))


(define-syntax dolist
  (syntax-rules ()
    [(_ (iter ls) body ...)
     (for-each (lambda (iter) (begin body ...)) ls)]))

(define (op? x)
  (memq x '(+ - * / < <= >= > = eq?)))


;; --------------- data structures ---------------
;; AST nodes

(define-type Node
  (U Const
     Sym
     Var
     Attribute
     Fun
     App
     RecordDef
     VectorDef
     Def
     Import
     Assign
     Seq
     Return
     If
     Op))

(struct: Const ([obj : (U Symbol Number String)])
         #:transparent)
(struct: Sym ([name : Symbol])
         #:transparent)
(struct: Var ([name : Symbol])
         #:transparent)
(struct: Attribute ([segs : (Listof Var)])
         #:transparent)
(struct: Fun ([param : Node]
              [body : Seq])
         #:transparent)
(struct: App ([fun : Node]
              [arg : Node])
         #:transparent)
(struct: RecordDef ([name : Node]
                    [fields : (Listof Node)])
         #:transparent)
(struct: VectorDef ([elems : (Listof Node)])
         #:transparent)
(struct: Def ([pattern : Node]
              [value : Node])
         #:transparent)
(struct: Import ([origin : Node]
                 [names : (Listof Var)])
         #:transparent)
(struct: Assign ([pattern : Node]
                 [value : Node])
         #:transparent)
(struct: Seq ([statements : (Listof Node)])
         #:transparent)
(struct: Return ([value : Node])
         #:transparent)
(struct: If ([test : Node]
             [then : Node]
             [else : Node])
         #:transparent)
(struct: Op ([op : Var]
             [e1 : Node]
             [e2 : Node])
         #:transparent)


(define-type Value
  (U Const
     Closure
     Record
     Vector
     Symbol
     Number
     String
     Boolean
     False
     Node))

;; interpreter's internal structures
(struct: Closure ([fun : FunValue]
                  [env : Env])
         #:transparent)
(struct: FunValue ([param : Value]
                   [body : Seq])
         #:transparent)
(struct: Record ([name : Node]
                 [fields : (Listof Node)]
                 [table : (HashTable Symbol Value)])
         #:transparent)
(struct: Vector ([elems : (Listof Value)])
         #:transparent)


;; --------------- parser and unparser ---------------
;; parse and unparse into record types

(: parse (Any -> Node))
(define (parse sexp)
  (match sexp
    [(? number? x) (Const x)]
    [(? string? x) (Const x)]
    [(? symbol? x)
     (let ([segs (string-split (symbol->string x) ".")])
       (cond
        [(= 1 (length segs))
         (Var x)]
        [else
         (let ([segs (map Var (map string->symbol segs))])
          (Attribute segs))]))]
    [`(quote ,(? symbol? x)) (Sym x)]
    [`(fn (,params ...) ,body ...)
     (Fun (parse `(record params ,@params)) (Seq (map parse body)))]
    [(list (? op? op) e1 e2)
     (Op (Var op) (parse e1) (parse e2))]
    [`(if ,test ,then ,else)
     (If (parse test) (parse then) (parse else))]
    [`(begin ,statements ...)
     (Seq (map parse statements))]
    [`(return ,value)
     (Return (parse value))]
    [`(defn (,f ,params ...) ,body ...)
     (Def (parse f) (parse `(fn ,params ,@body)))]
    [`(:+ ,pattern ,value)
     (Def (parse pattern) (parse value))]
    [`(<- ,pattern ,value)
     (Assign (parse pattern) (parse value))]
    [`(record ,name ,fields ...)
     (RecordDef (parse name) (map parse fields))]
    [`(vec ,elems ...)
     (VectorDef (map parse elems))]
    [`(import ,origin (,(? symbol? names) ...))
     (cond
       [(andmap symbol? names)
        (Import (parse origin) (map Var names))]
       [else
        (error 'parse)])]
    [`(,f ,args ...)  ; application must stay last
     (cond
      [(andmap def-form? args)
       (App (parse f) (parse `(record args ,@args)))]
      [(andmap (negate def-form?) args)
       (App (parse f) (parse `(vec ,@args)))]
      [else
       (abort 'parse
              "application must either be all keyword args"
              " or all positional args, no mixture please")])]
    ))


(: def-form? (Any -> Boolean))
(define (def-form? x)
  (and (list? x)
       (= 3 (length x))
       (eq? ':+ (car x))))


(: unparse (Any -> Any))
(define (unparse t)
  (match t
    [(Const obj) obj]
    [(Var name) name]
    [(Attribute segs)
     (let ([seg-strings (map symbol->string (map Var-name segs))])
       (string->symbol (string-join seg-strings ".")))]
    [(Sym name) name]
    [(Fun x body)
     `(fn ,(unparse x) ,(unparse body))]
    [(App e1 e2)
     `(,(unparse e1) ,(unparse e2))]
    [(Op op e1 e2)
     `(,(unparse op) ,(unparse e1) ,(unparse e2))]
    [(RecordDef name fields)
     `(record ,(unparse name) ,@(map unparse fields))]
    [(Record name fields table)
     (let ([fs (hash-map table
                         (lambda: ([k : Symbol] [v : Value])
                           `(:+ ,(unparse k) ,(unparse v))))])
       `(record ,(unparse name) ,@fs))]
    [(VectorDef elems)
     `(vecdef ,@(map unparse elems))]
    [(Vector elems)
     `(vec ,@(map unparse elems))]
    [(Import origin names)
     `(import ,(unparse origin) ,(map unparse names))]
    [(Def pattern value)
     `(:+ ,(unparse pattern) ,(unparse value))]
    [(Assign pattern value)
     `(<- ,(unparse pattern) ,(unparse value))]
    [(Seq statements)
     (let ([sts (map unparse statements)])
       (cond
        [(= 1 (length sts))
         sts]
        [else
         `(begin ,@sts)]))]
    [(Return value)
     `(return ,(unparse value))]
    [(If test then else)
     `(if ,(unparse test) ,(unparse then) ,(unparse else))]
    [(Closure fun env)
     (unparse fun)]
    [other other]
    ))

;; (parse '(f (:+ x 1) (:+ y 2)))
;; (parse '(f x y))
;; (parse '(f x (:+ y 2)))
;; (unparse (parse '(vec 1 2 3)))
;; (unparse (parse '(f (:+ x 1) (:+ y 2))))
;; (unparse (parse '(:+ (f x (:+ y 1)) (+ x y))))
;; (unparse (parse '(import r x y z)))
;; (unparse (parse 'x.y.z.w))
;; (unparse (parse '(record r1 f1 (<- f2 0))))
;; (unparse (parse '(return 1)))
;; (unparse (parse '(f 'x)))
;; (parse '(op + 1 2))
;; (unparse (parse '(begin x y z)))
;; (unparse (parse '(:+ x 1)))
;; (unparse (parse '(record x (1 2))))
;; (unparse (parse '(fn (record x (1 2)) "hi")))


;; --------------- symbol table (environment) ---------------
;; environment is a linked list of hash tables
;; deliberately not using pure data structures
;; side effects make quite some things easier

(struct: Env ([table : (HashTable Symbol Value)]
              [parent : (Option Env)])
  #:transparent)

(: hash-none ( -> False))
(define hash-none (lambda () #f))

(define (empty-env) (Env (make-hasheq) #f))

(: env-extend (Env -> Env))
(define (env-extend env)
  (Env (make-hasheq) env))

(: env-put! (Env Symbol Value -> Void))
(define (env-put! env key value)
  (cond
   [(not (symbol? key))
    (abort 'lookup-local "only accept symbols, but got: " key)]
   [else
    (hash-set! (Env-table env) key value)]))

(: lookup-local (Symbol Env -> (Option Value)))
(define (lookup-local key env)
  (hash-ref (Env-table env) key hash-none))

(: lookup (Symbol Env -> (Option Value)))
(define (lookup key env)
  (let ([val (lookup-local key env)]
        [parent (Env-parent env)])
    (cond
      [(not val)
       (cond
         [parent
          (lookup key parent)]
         [else #f])]
      [else val])))

(: find-defining-env (Symbol Env -> (Option Env)))
(define (find-defining-env key env)
  (let ([val (lookup-local key env)]
        [parent (Env-parent env)])
    (cond
      [(not val)
       (cond
         [parent
          (find-defining-env key parent)]
         [else #f])]
      [else env])))

;; (define e1 (env-extend env0))
;; (env-put! e1 'x 2)


(define constants
  `(true false))

(define (env0) (empty-env))


;; ---------------- main interpreter -----------------
(: interp1 (Node Env -> Value))
(define (interp1 exp env)
  (match exp
    [(Const obj) obj]
    [(Sym name) name]
    [(Var name)
     (cond
      [(memq name constants) name]
      [else
       (let ([val (lookup name env)])
         (cond
          [(not val)
           (abort 'interp "unbound variable: " name)]
          [else val]))])]
    [(Fun x body)
     (Closure (FunValue (interp1 x env) body) env)]
    [(App e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       (match v1
         [(Closure (FunValue pattern body) env1)
          (let ([env+ (env-extend env1)])
            (bind-params pattern v2 env+)
            (interp1 body env+))]))]
    [(Op op e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       (cond
         [(eq? (Var-name op) 'eq?)
          (eq? v1 v2)]
         [(and (real? v1) (real? v2))
          (case (Var-name op)
            [(+) (+ v1 v2)]
            [(-) (- v1 v2)]
            [(*) (* v1 v2)]
            [(/) (/ v1 v2)]
            [(>) (> v1 v2)]
            [(<) (< v1 v2)]
            [(>=) (>= v1 v2)]
            [(<=) (<= v1 v2)]
            [(=) (= v1 v2)]
            [else
             (abort 'interp "undefined operator on numbers: " op)])]
         [else
          (abort 'interp "can only operate on numbers, but got: " v1 "," v2)]))]
    [(If test then else)
     (let ([tv (interp1 test env)])
       (if tv
           (interp1 then env)
           (interp1 else env)))]
    [(Def pattern value)
     (let ([v (interp1 value env)])
       (bind pattern v env)
       'void)]
    [(Assign lhs value)
     (let ([v (interp1 value env)])
       (match lhs
         [(Var x)
          (let ([env-def (find-defining-env x env)])
            (cond
              [env-def
               (env-put! env-def x v)
               'void]
              [else
               (abort 'assign
                      "lhs of assignment if not bound: "
                      (unparse lhs))]))]))]
    [(Seq statements)
     (let loop ([statements statements])
       (let ([s0 (first statements)]
             [ss (rest statements)])
         (cond
          [(Return? s0)
           (interp1 (Return-value s0) env)]
          [(null? ss)
           (interp1 s0 env)]
          [else
           (interp1 s0 env)
           (loop ss)])))]
    [(RecordDef (Var name) fields)
     (let ([r (new-record (RecordDef (Var name) fields) env #f)])
       (env-put! env name r)
       r)]
    [(VectorDef elems)
     (let ([res (map (lambda: ([x : Node]) (interp1 x env)) elems)])
       (Vector res))]
    [(Attribute segs)
     (let ([r (interp1 (first segs) env)])
       (cond
         [(Record? r)
          (record-ref r (Attribute (rest segs)))]
         [else
          (abort 'interp "trying to access fields of non-record: " r)]))]
    [(Import origin names)
     (let ([r (interp1 origin env)])
       (cond
        [(Record? r)
         (for-each (lambda: ([name : Var])
                     (env-put! env
                           (Var-name name)
                           (record-ref r name)))
                   names)
         'void]
        [else
         (abort 'interp "trying to import from non-record: " r)]))]
    ))


(: record-ref (Record (U Var Attribute) -> Value))
(define (record-ref record accessor)
  (match accessor
    [(Var name)
     (hash-ref (Record-table record) name)]
    [(Attribute segs)
     (let: loop : Value ([segs : (Listof Var) segs] [value : Value record])
       (cond
        [(null? segs) value]
        [(Record? value)
         (let ([next-val (hash-ref (Record-table value)
                                   (Var-name (first segs))
                                   hash-none)])
           (cond
            [(not next-val)
             (abort 'record-ref "attr not exist: " (first segs))]
            [else
             (loop (rest segs) next-val)]))]
        [else
         (abort 'record-ref
                "take attr of a non-record: "
                (unparse value))]))]
    [else
     (abort 'record-ref
            "access with non-var and non-attr: "
            (unparse accessor))]))


;; general pattern binder
;; can be arbitrarily nested
(: bind ((U Node Value) Value Env -> Void))
(define (bind v1 v2 env)
  (match (list v1 v2)
    [(list (RecordDef name1 fields1) v2)
     (bind (new-record (RecordDef name1 fields1) env #t) v2 env)]
    ;; records
    [(list (Record name1 fields1 table1)
           (Record name2 fields2 table2))
     (hash-for-each
      table1
      (lambda: ([k1 : Symbol] [v1 : Value])
        (let ([v2 (hash-ref table2 k1 hash-none)])
          (cond
           [v2
            (bind v1 v2 env)]
           [else
            (abort 'bind "unbound key in rhs: " k1)]))))]
    ;; vectors
    [(list (VectorDef names)
           (Vector values))
     (cond
      [(= (length names) (length values))
       (let loop ([vec1 names]
                  [vec2 values])
         (cond
          [(null? vec1) (void)]
          [else
           (bind (first vec1) (first vec2) env)
           (loop (rest vec1) (rest vec2))]))]
      [else
       (abort 'bind
              "incorrect number of arguments\n"
              " expected: " (length names)
              " got: " (length values))])]
    [(list (Record name1 fields1 table1)
           (Vector elems))
     (cond
      [(= (length fields1) (length elems))
       (let loop ([vec1 fields1]
                  [vec2 elems])
         (cond
          [(null? vec1) (void)]
          [else
           (bind (first vec1) (first vec2) env)
           (loop (rest vec1) (rest vec2))]))]
      [else
       (abort 'bind
              "incorrect number of arguments\n"
              " expected: " (length fields1)
              " got: " (length elems))])]
    ;; base case
    [(list (Def x y) v2)
     (bind x v2 env)]
    [(list (Var x) v2)
     (let ([existing (lookup-local x env)])
       (cond
        [existing
         (abort 'bind
                "redefining: " x
                " was defined as: " (unparse existing))]
        [else
         (env-put! env x v2)]))]))


;; parameter binder for functions
;; only slightly different from bind
;; but separate it out in order to be clear
(: bind-params ((U Node Value) Value Env -> Void))
(define (bind-params v1 v2 env)
  (match (list v1 v2)
    [(list (RecordDef name1 fields1)
           (Record name2 fields2 table2))
     (bind-params (new-record (RecordDef name1 fields1) env #t) v2 env)]
    ;; records
    [(list (Record name1 fields1 table1)
           (Record name2 fields2 table2))
     (hash-for-each
      table1
      (lambda: ([k1 : Symbol] [v1 : Value])
        (let ([v2 (hash-ref table2 k1 hash-none)])
          (cond
           [v2
            (env-put! env k1 v2)]
           [v1
            (env-put! env k1 v1)]
           [else
            (abort 'bind-params "unbound key in rhs: " k1)]))))]
    ;; vectors
    [(list (VectorDef names)
           (Vector values))
     (cond
      [(= (length names) (length values))
       (let loop ([vec1 names]
                  [vec2 values])
         (cond
          [(null? vec1) (void)]
          [else
           (bind-params (first vec1) (first vec2) env)
           (loop (rest vec1) (rest vec2))]))]
      [else
       (abort 'bind-params
              "incorrect number of arguments\n"
              " expected: " (length names)
              " got: " (length values))])]
    ;; record <- vector, positional assignment
    [(list (Record name1 fields1 table1)
           (Vector elems))
     (cond
      [(= (length fields1) (length elems))
       (let loop ([vec1 fields1]
                  [vec2 elems])
         (cond
          [(null? vec1) (void)]
          [else
           (bind-params (first vec1) (first vec2) env)
           (loop (rest vec1) (rest vec2))]))]
      [else
       (abort 'bind-params
              "incorrect number of arguments\n"
              " expected: " (length fields1)
              " got: " (length elems))])]
    ;; base case
    [(list (Var x) v2)
     (let ([existing (lookup-local x env)])
       (cond
        [existing
         (abort 'bind-params
                "redefining: " x
                " was defined as: " (unparse existing))]
        [else
         (env-put! env x v2)]))]))

(: find-name (Node -> Var))
(define (find-name exp)
  (match exp
   [(Var x) (Var x)]
   [(Def (Var x) value)
    (Var x)]
   [other
    (abort 'find-name "only accepts Var and Def, but got: " exp)]))


(: new-record (RecordDef Env Boolean -> Record))
(define (new-record desc env pattern?)
  (match desc
    [(RecordDef name fields)
     (let: ([table : (HashTable Symbol Value) (make-hasheq)])
       (fill-record-table desc table env pattern?)
       (Record name fields table))]))


(: fill-record-table (RecordDef (HashTable Symbol Value) Env Boolean -> Void))
(define (fill-record-table desc table env pattern?)
  (dolist (f (RecordDef-fields desc))
    (match f
      [(Var x)
       (hash-set! table x #f)]
      [(Def (Var x) value)
       (cond
        [pattern?
         (hash-set! table x value)]
        [else
         (let ([v (interp1 value env)])
           (hash-set! table x v))])]
      [other (void)])))


(define (interp exp)
  (interp1 (parse exp) (env0)))


(define (view exp)
  (unparse (interp exp)))

;; (require "v1-tests.rkt")
