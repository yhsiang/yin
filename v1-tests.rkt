(test
 "begin with only one statement"
 1
 '(begin
    1))

(test 
 "single positional arg"
 1
 '(begin
    (:+ (f x) x)
    (f 1)))

(test
 "function positional param single keyword arg"
 1
 '(begin
    (:+ (f x) x)
    (f (:+ x 1))))

(test
 "function with multiple positional args"
 15
 '(begin
    (:+ f (fn (x y) (* x y)))
    (f 3 5)))

(test
 "function mutiple positional param with keyword args same order"
 30
 '(begin
    (:+ (f x y z) (* (* x y) z))
    (f (:+ x 2)
       (:+ y 3)
       (:+ z 5))))

(test
 "function mutiple positional param with keyword args (different order)"
 30
 '(begin
    (:+ (f x y z) (* (* x y) z))
    (f (:+ y 3)
       (:+ z 5)
       (:+ x 2))))

(test
 "function mutiple keyword params with keyword args (full)"
 30
 '(begin
    (:+ (f x (:+ y 3) (:+ z 7)) (* (* x y) z))
    (f (:+ y 3)
       (:+ z 5)
       (:+ x 2))))

(test
 "function mutiple keyword params with keyword args (missing z)"
 42
 '(begin
    (:+ (f x (:+ y 3) (:+ z 7)) (* (* x y) z))
    (f (:+ y 3)
       (:+ x 2))))

(test
 "mixed parameter full keyword args"
 6
 '(begin
    (:+ (f x (:+ y 1))
        (* x y))
    (f (:+ x 2) (:+ y 3))))

(test
 "mixed parameters default keyword arg for y"
 2
 '(begin
    (:+ (f x (:+ y 1))
        (* x y))
    (f (:+ x 2))))

(test
 "function access field of arbitrary record arg"
 42
 '(begin
    (:+ f (fn (x) x.a))
    (:+ o (rec any (:+ a 42)))
    (f o)))

(test
 "assignment in true branch"
 "true"
 '(begin
    (:+ x 1)
    (if (< x 2)
        (<- x "true")
        (<- x "false"))
    x))

(test
 "assignment in true branch"
 "false"
 '(begin
    (:+ x 5)
    (if (< x 2)
        (<- x "true")
        (<- x "false"))
    x))

(test
 "import from record simple"
 1
 '(begin
    (rec r1 (:+ x 1) (:+ y 2))
    (import r1 x)
    x))

(test
 "import from record nested"
 1
 '(begin
    (rec r1 (:+ x 1))
    (rec r2 (:+ y r1))
    (import r2.y x)
    x))

(test
 "import from record nested"
 1
 '(begin
    (rec r1 (:+ x 1))
    (rec r2 (:+ y r1))
    (import r2 y)
    (import y x)
    x))

(test 
 "import function and fields"
 30
  '(begin
     (rec r1 (:+ x 2) (:+ y 3))
     (rec r2
          (:+ z 5)
          (:+ f (fn (x y z) (* (* x y) z))))
     (import r1 x y)
     (import r2 f z)
     (f x y z)))

(test
 "import inside function"
 6
 '(begin
    (rec r1
         (:+ x 1)
         (:+ y 2))
    (:+ (f z)
        (import r1 y)
        (* y z))
    (f 3)))

(test
 "return in middle"
 3
 '(begin
    (:+ x 1)
    (:+ y 2)
    (return (+ x y))
    10))


(test
 "defs to same var in different branches"
 "yes"
 '(begin
    (:+ x 1)
    (if (< x 2)
        (:+ s "yes")
        (:+ s "no"))
    s))

(test
 "defs to same var in different branches"
 "no"
 '(begin
    (:+ x 5)
    (if (< x 2)
        (:+ s "yes")
        (:+ s "no"))
    s))

(test
 "defs to same func name in different branches (true)"
 8
 '(begin
    (:+ x 1)
    (if (< x 2)
        (begin
          (:+ g (fn (y) (* y 2))))
        (begin
          (:+ g (fn (y) (/ y 2)))))
    (g 4)))

(test
 "defs to same func name in different branches (false)"
 2
 '(begin
    (:+ x 5)
    (if (< x 2)
        (begin
          (:+ g (fn (y) (* y 2))))
        (begin
          (:+ g (fn (y) (/ y 2)))))
    (g 4)))

(test
 "defs to same func name in different branches (func syntax, true)"
 8
 '(begin
    (:+ x 1)
    (if (< x 2)
        (begin
          (:+ (g y) (* y 2)))
        (begin
          (:+ (g y) (/ y 2))))
    (g 4)))

(test
 "defs to same func name in different branches (func syntax, false)"
 2
 '(begin
    (:+ x 5)
    (if (< x 2)
        (begin
          (:+ (g y) (* y 2)))
        (begin
          (:+ (g y) (/ y 2))))
    (g 4)))

(test
 "assignment inside function into environment"
 42
 '(begin
    (:+ x 1)
    (:+ f (fn (y) (<- x y)))
    (f 42)
    x))


;; even & odd mutural recursion
;; (define (even x) (if (= x 0) #t (odd (- x 1))))
;; (define (odd x) (if (= x 0) #f (even (- x 1))))
(test
 "mutural recursion (even 9 = false)"
 'false
 '(begin
    (:+ not (fn (x) (if (eq? x true) false true)))
    (:+ even (fn (x) (if (= x 0) true (odd (- x 1)))))
    (:+ odd (fn (x) (if (= x 0) false (even (- x 1)))))
    (even 9)))

(test
 "mutural recursion (even 9 = true)"
 'true
 '(begin
    (:+ not (fn (x) (if (eq? x true) false true)))
    (:+ even (fn (x) (if (= x 0) true (odd (- x 1)))))
    (:+ odd (fn (x) (if (= x 0) false (even (- x 1)))))
    (even 100)))


(test
 "definition of not"
 'false
 '(begin
    (:+ not (fn (x) (if (eq? x true) false true)))
    (not true)))


(test
 "direct recursion (fact 5)"
 120
 '(begin
    (:+ fact (fn (x) (if (= x 0) 1 (* x (fact (- x 1))))))
    (fact 5)))

(test
 "conditional (simple)"
 "<"
 '(begin
    (:+ x 2)
    (:+ f (fn (x) (* x 2)))
    (if (< (f x) 5) "<" ">=")))

(test
 "conditional (simple)"
 ">="
 '(begin
    (:+ x 3)
    (:+ f (fn (x) (* x 2)))
    (if (< (f x) 5) "<" ">=")))

(test
 "comparison operator"
 "no"
 '(if (> 1 2) "yes" "no"))

(test
 "comparison operator"
 "yes"
 '(if (< 1 2) "yes" "no"))

(test
 "function goes through identity function"
 6 
 '(begin
   (:+ f (fn (x) x))
   (:+ g (fn (x) (* x 2)))
   (:+ fg (f g))
   (fg 3)))

(test
 "function stored in record field"
 10
 '(begin
    (:+ r1 (rec something (:+ x (fn (y) (* y 2)))))
    (r1.x 5)))

(test
 "function field pass to function and apply"
 10
 '(begin
    (:+ (bar x) (x.foo 5))
    (bar (rec something (:+ foo (fn (y) (* y 2)))))))

