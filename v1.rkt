;; The Y Programming Language (Version 1)


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
           (printf "~n[fail]!~n  expected: ~a~n  got: ~a~n  input: ~a~n"
                   expected result exp)])))]))


;; special value for representing "nothing"
;; (avoid using #f for nothing because nothing is not boolean)
(define nothing 'nothing)
(define (nothing? x) (eq? x 'nothing))
(define (something? x) (not (eq? x 'nothing)))

(define (op? x)
  (memq x '(+ - * / < <= >= > = eq?)))


;; --------------- data structures ---------------
;; AST nodes
(struct Const (obj) #:transparent)
(struct Symbol (name) #:transparent)
(struct Var (name) #:transparent)
(struct Attribute (segs) #:transparent)
(struct Fun (param body) #:transparent)
(struct App (fun arg) #:transparent)
(struct RecordDef (name fields) #:transparent)
(struct Def (pattern value) #:transparent)
(struct Import (origin names) #:transparent)
(struct Assign (pattern value) #:transparent)
(struct Seq (statements) #:transparent)
(struct Return (value) #:transparent)
(struct If (test then else) #:transparent)
(struct Op (op e1 e2) #:transparent)

;; interpreter's internal structures
(struct Closure (fun env) #:transparent)
(struct Record (name fields table) #:transparent)
(struct Vector (elems) #:transparent)


;; --------------- parser and unparser ---------------
;; parse and unparse into record types

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
         (let ([seg-symbols (map string->symbol segs)])
          (Attribute seg-symbols))]))]
    [`(quote ,x) (Symbol x)]
    [`(fn (,params ...) ,body ...)
     (Fun (parse `(record params ,@params)) (Seq (map parse body)))]
    [(list (? op? op) e1 e2)
     (Op (parse op) (parse e1) (parse e2))]
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
     (Vector (map parse elems))]
    [`(import ,origin ,names ...)
     (Import (parse origin) (map parse names))]
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

(define (def-form? x)
  (and (list? x)
       (= 3 (length x))
       (eq? ':+ (car x))))


(define (unparse t)
  (match t
    [(Const obj) obj]
    [(Var name) name]
    [(Attribute segs)
     (let ([seg-strings (map symbol->string segs)])
       (string->symbol (string-join seg-strings ".")))]
    [(Symbol name) name]
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
                         (lambda (k v)
                           `(:+ ,(unparse k) ,(unparse v))))])
       `(record ,(unparse name) ,@fs))]
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

(struct Env (table parent) #:transparent)

(define (empty-env) (Env (make-hasheq) nothing))

(define (env-extend env)
  (Env (make-hasheq) env))

(define (env-put! env key value)
  (cond
   [(not (symbol? key))
    (abort 'lookup-local "only accept symbols, but got: " key)]
   [else
    (hash-set! (Env-table env) key value)]))

(define (lookup-local key env)
  (cond
   [(not (symbol? key))
    (abort 'lookup-local "only accept symbols, but got: " key)]
   [else
    (hash-ref (Env-table env) key 'nothing)]))

(define (lookup key env)
  (cond
   [(not (symbol? key))
    (abort 'lookup "only accept symbols, but got: " key)]
   [(nothing? env) nothing]
   [else
    (let ([val (lookup-local key env)])
      (cond
       [(nothing? val)
        (lookup key (Env-parent env))]
       [else val]))]))

(define (find-defining-env key env)
  (cond
   [(not (symbol? key))
    (abort 'find-defining-env "only accept symbols, but got: " key)]
   [(nothing? env) nothing]
   [else
    (let ([val (lookup-local key env)])
      (cond
       [(nothing? val)
        (find-defining-env key (Env-parent env))]
       [else env]))]))

;; (define e1 (env-extend env0))
;; (env-put! e1 'x 2)


(define constants
  `(true false))

(define (env0) (empty-env))


;; ---------------- main interpreter -----------------
(define (interp1 exp env)
  (match exp
    [(Const obj) obj]
    [(Symbol name) name]
    [(Var name)
     (cond
      [(memq name constants) name]
      [else
       (let ([val (lookup name env)])
         (cond
          [(nothing? val)
           (abort 'interp "unbound variable: " name)]
          [else val]))])]
    [(Fun x body)
     (Closure (Fun (interp1 x env) body) env)]
    [(App e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       (match v1
         [(Closure (Fun pattern body) env1)
          (let ([env+ (env-extend env1)])
            (bind-params pattern v2 env+)
            (interp1 body env+))]))]
    [(Op op e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       ((eval (Var-name op)) v1 v2))]
    [(If test then else)
     (let ([tv (interp1 test env)])
       (if tv
           (interp1 then env)
           (interp1 else env)))]
    [(Def pattern value)
     (let ([v (interp1 value env)])
       (bind pattern v env))]
    [(Assign lhs value)
     (let ([v (interp1 value env)])
       (match lhs
         [(Var x)
          (let ([env-def (find-defining-env x env)])
            (env-put! env-def x v))]))]
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
     (let ([r (new-record exp env #f)])
       (env-put! env name r)
       r)]
    [(Vector elems)
     (let ([res (map (lambda (x) (interp1 x env)) elems)])
       (Vector res))]
    [(Attribute segs)
     (let* ([s0 (first segs)]
            [v0 (lookup s0 env)])
       (cond
        [(Record? v0)
         (let loop ([segs (rest segs)]
                    [value v0])
           (cond
            [(null? segs) value]
            [(Record? value)
             (let ([next-val (hash-ref (Record-table value)
                                       (first segs)
                                       nothing)])
               (cond
                [(nothing? next-val)
                 (abort 'interp1 "attr not exist: " (first segs))]
                [else
                 (loop (rest segs)
                       next-val)]))]
            [else
             (abort 'interp1 "take attr of a non-table: " (unparse v0))]))]
        [else
         (abort 'interp1 "take attr of a non-table: " (unparse v0))]))]
    [(Import origin names)
     (let ([vo (interp1 origin env)])
       (cond
        [(Record? vo)
         (let loop ([names names])
           (cond
            [(null? names) (void)]
            [else
             (let ([n0 (first names)])
               (env-put! env (Var-name n0) (record-ref vo n0))
               (loop (rest names)))]))]
        [else
         (abort 'interp "trying to import from non-record: " vo)]))]
    ))


;; general pattern binder
;; can be arbitrarily nested
(define (bind v1 v2 env)
  (match (list v1 v2)
    [(list (RecordDef name1 fields1) v2)
     (bind (new-record v1 env #t) v2 env)]
    ;; records
    [(list (Record name1 fields1 table1)
           (Record name2 fields2 table2))
     (hash-for-each
      table1
      (lambda (k1 v1)
        (let ([v2 (hash-ref table2 k1 nothing)])
          (cond
           [(something? v2)
            (bind v1 v2 env)]
           [else
            (abort 'bind "unbound key in rhs: " k1)]))))]
    ;; vectors
    [(list (Vector names)
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
    [(list (Var x) v2)
     (let ([existing (lookup-local x env)])
       (cond
        [(something? existing)
         (abort 'bind
                "redefining: " x
                " was defined as: " (unparse existing))]
        [else
         (env-put! env x v2)]))]))


;; parameter binder for functions
;; only slightly different from bind
;; but separate it out in order to be clear
(define (bind-params v1 v2 env)
  (match (list v1 v2)
    [(list (RecordDef name1 fields1)
           (Record name2 fields2 table2))
     (bind-params (new-record v1 env #t) v2 env)]
    ;; records
    [(list (Record name1 fields1 table1)
           (Record name2 fields2 table2))
     (hash-for-each
      table1
      (lambda (k1 v1)
        (let ([v2 (hash-ref table2 k1 nothing)])
          (cond
           [(something? v2)
            (env-put! env k1 v2)]
           [(something? v1)
            (env-put! env k1 v1)]
           [else
            (abort 'bind-params "unbound key in rhs: " k1)]))))]
    ;; vectors
    [(list (Vector names)
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
        [(something? existing)
         (abort 'bind-params
                "redefining: " x
                " was defined as: " (unparse existing))]
        [else
         (env-put! env x v2)]))]))


(define (record-ref record name/attr)
  (cond
   [(Var? name/attr)
    (hash-ref (Record-table record) (Var-name name/attr))]
   [(Attribute? name/attr)
    (let* ([segs (Attribute-segs name/attr)]
           [s0 (first name/attr)]
           [v0 record])
      (cond
       [(Record? v0)
        (let loop ([segs (rest segs)]
                   [value v0])
          (cond
           [(null? segs) value]
           [(Record? value)
            (let ([next-val (hash-ref (Record-table value)
                                      (first segs)
                                      nothing)])
              (cond
               [(nothing? next-val)
                (abort 'interp1 "attr not exist: " (first segs))]
               [else
                (loop (rest segs)
                      next-val)]))]
           [else
            (abort 'interp1
                   "take attr of a non-table: "
                   (unparse v0))]))]
       [else
        (abort 'interp1
               "take attr of a non-table: "
               (unparse v0))]))]
   [else
    (abort 'record-ref
           "access with non-var and non-attr: "
           (unparse name/attr))]))


(define (find-name exp)
  (match exp
   [(Var x) exp]
   [(Def (Var x) value)
    (Var x)]))


(define (new-record desc env pattern?)
  (match desc
    [(RecordDef (Var name) fields)
     (let ([table (make-hasheq)]
           [field-names (map find-name fields)])
       (fill-record-table desc table env pattern?)
       (Record name field-names table))]))


(define (fill-record-table desc table env pattern?)
  (let loop ([fields (RecordDef-fields desc)])
    (cond
     [(null? fields) (void)]
     [else
      (let ([f0 (first fields)])
        (match f0
          [(Var x)
           (hash-set! table x 'nothing)]
          [(Def (Var x) value)
           (cond
            [pattern?
             (hash-set! table x value)]
            [else
             (let ([v (interp1 value env)])
               (hash-set! table x v))])]
          [other (void)])
        (loop (rest fields)))])))


(define (interp exp)
  (interp1 (parse exp) (env0)))


(define (view exp)
  (unparse (interp exp)))

(load "v1-tests.rkt")
