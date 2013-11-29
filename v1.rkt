;; The Y Programming Language (Version 1)


;; --------------- utilities ---------------
(define (fatal who . args)
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


; special value for representing "nothing"
; (avoid using #f for nothing because nothing is not boolean)
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
(struct Fun (param body) #:transparent)
(struct App (fun arg) #:transparent)
(struct Record (name fields) #:transparent)
(struct Define (pattern value) #:transparent)
(struct Assign (pattern value) #:transparent)
(struct Seq (statements) #:transparent)
(struct Return (value) #:transparent)
(struct If (test then else) #:transparent)
(struct Op (op e1 e2) #:transparent)

;; closure
(struct Closure (fun env) #:transparent)


;; --------------- parser and unparser ---------------
;; parse and unparse into record types

(define (parse sexp)
  (match sexp
    [(? number? x) (Const x)]
    [(? string? x) (Const x)]
    [(? symbol? x) (Var x)]
    [`(quote ,x) (Symbol x)]
    [`(fn ,x ,body ...)
     (Fun (parse x) (Seq (map parse body)))]
    [(list (? op? op) e1 e2)
     (Op (parse op) (parse e1) (parse e2))]
    [`(if ,test ,then ,else)
     (If (parse test) (parse then) (parse else))]
    [`(begin ,statements ...)
     (Seq (map parse statements))]
    [`(return ,value)
     (Return (parse value))]
    [`(def ,pattern ,value)
     (Define (parse pattern) (parse value))]
    [`(<- ,pattern ,value)
     (Assign (parse pattern) (parse value))]
    [`(rec ,name ,fields)
     (Record (parse name) (map parse fields))]
    [`(,e1 ,e2)  ; must stay last
     (App (parse e1) (parse e2))]
    ))

(define (unparse t)
  (match t
    [(Const obj) obj]
    [(Var name) name]
    [(Symbol name) name]
    [(Fun x body)
     `(fn ,(unparse x) ,(unparse body))]
    [(App e1 e2)
     `(,(unparse e1) ,(unparse e2))]
    [(Op op e1 e2)
     `(,(unparse op) ,(unparse e1) ,(unparse e2))]
    [(Record name fields)
     `(rec ,(unparse name) ,(map unparse fields))]
    [(Define pattern value)
     `(def ,(unparse pattern) ,(unparse value))]
    [(Assign pattern value)
     `(<- ,(unparse pattern) ,(unparse value))]
    [(Seq statements)
     `(begin ,@(map unparse statements))]
    [(Return value)
     `(return ,(unparse value))]
    [(If test then else)
     `(if ,(unparse test) ,(unparse then) ,(unparse else))]
    [(Closure fun env)
     (unparse fun)]
    [other other]
    ))

;; (unparse (parse '(return 1)))
;; (unparse (parse '(f 'x)))
;; (parse '(op + 1 2))
;; (unparse (parse '(begin x y z)))
;; (unparse (parse '(def x 1)))
;; (unparse (parse '(rec x (1 2))))
;; (unparse (parse '(fn (rec x (1 2)) "hi")))


;; --------------- symbol table (environment) ---------------
;; environment is a linked list of hash tables
;; deliberately not using pure data structures
;; side effects make quite some things easier

(struct Env (table parent) #:transparent)

(define (empty-env) (Env (make-hasheq) nothing))

(define (env-extend env)
  (Env (make-hasheq) env))

(define (env-put env key value)
  (hash-set! (Env-table env) key value))

(define (lookup-local var env)
  (hash-ref (Env-table env) var 'nothing))

(define (lookup var env)
  (cond
   [(nothing? env) nothing]
   [else
    (let ([val (lookup-local var env)])
      (cond
       [(nothing? val)
        (lookup var (Env-parent env))]
       [else val]))]))

(define (find-defining-env var env)
  (cond
   [(nothing? env) nothing]
   [else
    (let ([val (lookup-local var env)])
      (cond
       [(nothing? val)
        (find-defining-env var (Env-parent env))]
       [else env]))]))

;; (define e1 (env-extend env0))
;; (env-put e1 'x 2)


(define constants
  `(true false))

(define (env0) (empty-env))

(define (interp1 exp env)
  (match exp
    [(Const obj)
     obj]
    [(Symbol name)
     name]
    [(Var name)
     (cond
      [(memq name constants) name]
      [else
       (let ([val (lookup name env)])
         (cond
          [(nothing? val)
           (fatal 'interp "unbound variable: " name)]
          [else
           val]))])]
    [(Fun x body)
     (Closure exp env)]
    [(App e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       (match v1
         [(Closure (Fun (Var x) body) env1)
          (let ([env2 (env-extend env1)])
            (env-put env2 x v2)
            (interp1 body env2))]
         [(? procedure? prim)
          (prim v2)]))]
    [(Op op e1 e2)
     (let ([v1 (interp1 e1 env)]
           [v2 (interp1 e2 env)])
       (match op
         [(Var x) ((eval x) v1 v2)]))]
    [(If test then else)
     (let ([tv (interp1 test env)])
       (if tv
           (interp1 then env)
           (interp1 else env)))]
    [(Define lhs value)
     (match lhs
       [(Var x)
        (let ([existing (lookup-local x env)])
          (cond
           [(something? existing)
            (fatal 'interp
                   "redefining: " x
                   " was defined as: " (unparse existing))]
           [else
            (let ([v (interp1 value env)])
              (env-put env x v))]))]
       [other
        (fatal 'interp
               "currently can only assign to variables"
               ", but got: " other)])]
    [(Assign lhs value)
     (let ([v (interp1 value env)])
       (match lhs
         [(Var x)
          (let ([env-def (find-defining-env x env)])
            (env-put env-def x v))]))]
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
    ))


(define (interp exp)
  (interp1 (parse exp) (env0)))



;; -------------- examples --------------
;; (interp '((fn x (x x)) (fn x (x x))))

(interp
 '(begin
   (def f (fn x x))
   (def g (fn x (* x 2)))
   (def fg (f g))
   (fg 3)))

(interp
 '(if (> 1 2) "yes" "no"))

(interp
 '(begin
    (def x 2)
    (def f (fn x (* x 2)))
    (if (< (f x) 5) "<" ">=")))

(interp
 '(begin
    (def fact (fn x (if (= x 0) 1 (* x (fact (- x 1))))))
    (fact 5)))

(interp
 '(begin
    (def not (fn x (if (eq? x true) false true)))
    (not true)))

;; even & odd mutural recursion
;; (define (even x) (if (= x 0) #t (odd (- x 1))))
;; (define (odd x) (if (= x 0) #f (even (- x 1))))
(interp
 '(begin
    (def not (fn x (if (eq? x true) false true)))
    (def even (fn x (if (= x 0) true (odd (- x 1)))))
    (def odd (fn x (if (= x 0) false (even (- x 1)))))
    (even 0)))

(interp
 '(begin
    (def x 1)
    (def f (fn y (<- x y)))
    (f 42)
    x))

(interp
 '(begin
    (def x 3)
    (if (< x 2)
        (def f "yes")
        (def f "no"))
    f))


(interp
 '(begin
    (def g
         (fn x
             (if (< x 2)
                 (begin
                   (def g (fn y (* y 2))))
                 (begin
                   (def g (fn y (/ y 2)))))
             (g 3)))
    (g 4)))


(interp
 '(begin
    1))

(interp
 '(begin
    (def x 1)
    (def y 2)
    (return (+ x y))
    10))
