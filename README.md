# SimpleInterpreter

[![auc][aucsvg]][auc] [![License][licensesvg]][license]

[aucsvg]: https://img.shields.io/badge/SimpleInterpreter-v0.0.12_alpha-brightgreen.svg
[auc]: https://github.com/lonelyenvoy/SimpleInterpreter

[licensesvg]: https://img.shields.io/badge/License-MIT-blue.svg
[license]: https://github.com/lonelyenvoy/SimpleInterpreter/blob/master/LICENSE

An interpreter for Simple language.

## Introduction

Simple © is a user-friendly high-level functional programing language running on JVM,
letting you build greater system with less code.

## Usage

```
java Simple
  (no argument)                 Enter REPL
  -v                            Show version
  -i filename1 [filename2 ...]  Read and evaluate code from input files
  -l filename1 [filename2 ...]  Do lexical analysis on code from input files
  -s filename1 [filename2 ...]  Do syntax analysis on code from input files
```

## Syntax

Simple has a syntax very similar to Common Lisp.

**1. Assigning value to variables:**
```lisp
(define numberOfCars 20)
```

**2. Arithmetic operation:**
```lisp
(+ 20 30)    ; 50
(- 40 50)    ; -10
(* 60 70)    ; 4200
(/ 80 90)    ; 0
(% 15 4)     ; 3
```

**3. Relations and logical operations:**
```lisp
(= 10 20)                    ; false
(< 10 20)                    ; true
(and (= 10 10) (< 10 20))    ; false
(or (= 10 10) (< 10 20))     ; true
(not false)                  ; true
```


**4. Manipulating Control flows:**
```lisp
(define price 20)
(define discount true)
(define price
  (if
    (= discount true)
    (/ price 2)            ; if discount is true
    price                  ; if discount is false
  )
)
```
The false clause of ```if``` statement can be omitted.

**5. Defining functions:**
```lisp
(define add (function (a b)
  (+ a b)
))
```
where ```add``` is the function name, ```a``` and ```b``` are function arguments,
```(+ a b)``` is the body (and the return value) of the function.

**6. Function calls:**
```lisp
(add 20 30)    ; 50
```

**7. Multiple expression in functions:**

```lisp
(define addOneAndTwo (function ()
  (do
    (define a 1)
    (define b 2)
    (+ a b)
  )
))
```
The last statement in ```do``` will be the return value.

**8. Manipulating lists:**

```lisp
(define alist (list 10 20 30))
(first alist)                     ; 10
(rest alist)                      ; (list 20 30)
(append alist (list 40))          ; (list 10 20 30 40)
(empty alist)                     ; false

(sort alist true)                 ; (list 10 20 30 40)
(sort alist false)                ; (list 40 30 20 10)

(define alist 
  (list
    (list 1 30)
    (list 2 20)
    (list 3 10)
  )
)
(sort alist true 0)               ; (list (list 1 30) (list 2 20) (list 3 10))
(sort alist true 1)               ; (list (list 3 10) (list 2 20) (list 1 30))
```

**9. Other built-in functions:**

```lisp
(random 0 10)    ; generate a random integer between [0, 10]
```

**10. Printing values:**
```lisp
(print 10)    ; 10
```
All the evaluation results of expressions will be automatically printed in REPL.
```print``` statement is only useful in code files.


## Types

```Number```, ```Boolean```, ```List```, ```Function``` are supported. All types are the subclass of ```Object```.

## Grammar

```
E -> id | number | BOOLEAN
E -> (E)
E -> (KEYWORD E*) | (FUNCTION E*)

BOOLEAN -> true | false
KEYWORD -> if | define | do | function | list
FUNCTION -> BUILTINFUNC | customfunc
BUILTINFUNC -> + | - | * | / | % 
             | and | or | not | = | < | > | <= | >= 
             | first | rest | append | empty | sort 
             | random | print
```

Or more formally in BNF:
```bnf
<expr>                ::= <ID> | <NUMBER> | <boolean>
                        | "(" <expr> ")"
                        | "(" <keyword> <chain-expr> ")" | "(" <function> <chain-expr> ")"
<chain-expr>          ::= <expr> | <expr> " " <chain-expr>
<boolean>             ::= "true" | "false"
<keyword>             ::= "if" | "define" | "do" | "function" | "list"
<function>            ::= <builtin-function> | <CUSTOM-FUNCTION>
<builtin-function>    ::= "+" | "-" | "*" | "/" | "%" 
                        | "and" | "or" | "not" | "=" | "<" | ">" | "<=" | ">=" 
                        | "first" | "rest" | "append" | "empty" | "sort" 
                        | "random" | "print"
```

## Contributing

Any improvement or bug-fixing is welcome. Create a [pull request](https://github.com/lonelyenvoy/SimpleInterpreter/pulls) when you are done.

## License

[The MIT License](https://github.com/lonelyenvoy/SimpleInterpreter/blob/master/LICENSE)
