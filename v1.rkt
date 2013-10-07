;;;;;;;;;;;;;;;;;;;;;;;;;;;; match patterns ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define-match-expander Symbol (syntax-rules () [(_ p) (? symbol? p)]))
(define-match-expander Number (syntax-rules () [(_ p) (? number? p)]))
(define-match-expander String (syntax-rules () [(_ p) (? string? p)]))
(define-match-expander Boolean (syntax-rules () [(_ p) (? boolean? p)]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; utilities ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define fatal
  (lambda (who . args)
    (printf "~s: " who)
    (for-each display args)
    (display "\n")
    (error 'infer "")))



;;;;;;;;;;;;;;;;;;;; lambda terms ;;;;;;;;;;;;;;;;;;;;;;;
(struct Const (obj) #:transparent)             ; constant
(struct Var (name) #:transparent)              ; variable
(struct Lam (var body) #:transparent)          ; lambda
(struct App (rator rand) #:transparent)        ; application

(struct Ann (var type) #:transparent)          ; annotation
(struct Struct (name fields) #:transparent)


(define parse-term
  (lambda (sexp)
    (match sexp
      [(Number x) (Const x)]
      [(String x) (Const x)]
      [(Boolean x) (Const x)]
      [(Symbol x) (Var x)]
      [`(lambda (,x) ,body) 
       (Lam (parse-term x) (parse-term body))]
      [`(,e1 ,e2) 
       (App (parse-term e1) (parse-term e2))]
      [`(,v : ,t)
       (Ann (parse-term v) (parse-term t))]
      [`(struct ,name ,e)
       (Struct name (map parse-term e))]
)))



(define unparse-term
  (lambda (t)
    (match t
      [(Const obj) obj]
      [(Var name) name]
      [(App e1 e2)
       `(,(unparse-term e1) ,(unparse-term e2))]
      [(Lam x body)
       `(lambda (,(unparse-term x)) ,(unparse-term body))]
      [(Ann var type)
       `(,(unparse-term var) : ,(unparse-term type))])))




;;;;;;;;;;;;;;;;;;;;;;;;;; environment ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define e0 '())
(define ext (lambda (x v s) `((,x . ,v) . ,s)))

;; lookup :: (Var * Env) -> Maybe Type
(define lookup
  (lambda (x env)
    (let ((slot (assq x env)))
      (cond
       [(not slot) #f]                  ; Nothing
       [else (cdr slot)]))))            ; Some Type



(parse-term '(struct A ([x : Bool])))

(unparse-term (parse-term '(lambda ([x : Int]) y)))

(parse-term '((lambda ("ok") x) 1))

