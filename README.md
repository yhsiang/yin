# The Y Programming Language

Copyright (C) 2013 Yin Wang. All rights reserved.

The Y Programming Language is in its design stage. Although its implementation
code is opensource, it is currently non-free and cannot be used by anybody
except Yin Wang. This arrangement is for the purpose of keeping the design of
language free of concerns from breaking legacy code.



### design goals

simple, intuitive, convenient, safe and fast



### implemented features

Although only accumulated a total of less than 30 hours of development time, Y
already includes the following features:

* first-class functions with positional and keyword arguments
* a powerful and universal pattern matching facility
* a convenient way of creating structured data without having to declare types



### to be implemented

The following is a list of things I'm thinking of implementing. I have already
implemented most of them elsewhere, but still have some design choices to make.


* a highly accurate but flexible type system

  The type system may require the programmers to write minimal amount of type
annotations, but it will not force the programmers to get out of their ways just
to make the type system happy. Despite of the flexibility, the type system will
be completely sound and will not let any type error to pass through the check.
No null pointer exceptions can ever happen. The type system will eventually be
highly refined, into the granularity of distinguishing values of numbers. Simple
theorem prover's capabilities may be provided.


* a dynamic compiler into machine code

  A compiler is not the proirity at this moment, but Y has efficiency of
compilation into existing hardware in mind. The compiler should be fast and
generate high-performance code. For safety needs, distribution of Y code will
not be machine code, but in some verifiable intermediate format. This format
will be translated into machine code at runtime.


* a runtime system that runs on bare metal

  Y will not be restricted by the current architecture or operating system
designs. Eventually Y's runtime system will become an operating system itself
and run on bare hardware, exploiting the concurrency. I'm also open to
possibilities of converting Y programs into hardware directly and let it serve
as a hardware description language (HDL).



### philosophy

Y will be designed not by piling features upon features, but by removing
weakness. But Y will also try to provide necessary facilities to make
programming convenient and productive.

Y will be free of "enthusiasts" and religion. Scientific methods will be used
throughout. Bad designs will be removed no matter who and where the ideas come
from. No authorities will be respected, including me.

The following quote from Alan J. Perlis best describes the motivation behind
this approach.


> "I think that it’s extraordinarily important that we in computer science keep
> fun in computing. When it started out it was an awful lot of fun. Of course
> the paying customers got shafted every now and then and after a while we began
> to take their complaints seriously. We began to feel as if we really were
> responsible for the successful error-free perfect use of these machines. I
> don’t think we are. I think we’re responsible for stretching them setting them
> off in new directions and keeping fun in the house. I hope the ﬁeld of
> computer science never loses its sense of fun. Above all I hope we don’t
> become missionaries. Don’t feel as if you’re Bible sales-men. The world has
> too many of those already. What you know about computing other people will
> learn. Don’t feel as if the key to successful computing is only in your hands.
> What’s in your hands I think and hope is intelligence: the ability to see the
> machine as more than when you were ﬁrst led up to it that you can make it
> more." -- Alan J. Perlis
