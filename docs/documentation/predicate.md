---
layout: default
title: Predicates String Syntax
id: predicate
section: documentation
---

<div markdown="1" class="span12">


### Basic Comparisons

*`=, eq`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is equal to the right-hand expression.

*`>=, =>`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is greater than or equal to the right-hand expression.

*`<=, =<`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is less than or equal to the right-hand expression.

*`>`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is greater than the right-hand expression.

*`<`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is less than the right-hand expression.

*`!=, <>`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression is not equal to the right-hand expression.

### Boolean Predicates

*`true`*

&nbsp;&nbsp;&nbsp;&nbsp;A predicate that always evaluates to TRUE.

*`false`*

&nbsp;&nbsp;&nbsp;&nbsp;A predicate that always evaluates to FALSE.

### Basic Compound Predicates

*`AND, &&`*

&nbsp;&nbsp;&nbsp;&nbsp;Logical AND.

*`OR, ||`*

&nbsp;&nbsp;&nbsp;&nbsp;Logical OR.

*`NOT`*

&nbsp;&nbsp;&nbsp;&nbsp;Logical NOT.

### String Comparisons

*`startsWith`*

&nbsp;&nbsp;&nbsp;&nbsp;The left-hand expression begins with the right-hand expression.

*`matches`*

&nbsp;&nbsp;&nbsp;&nbsp;The left hand expression matches right-hand expression using a full-text search.

*`contains`*

&nbsp;&nbsp;&nbsp;&nbsp; The use of `matches` is suggested for a large body of text, however `contains` should be used on short text fields such as a name or title.
### Other Predicates

*`missing`*

&nbsp;&nbsp;&nbsp;&nbsp;The left hand expression is missing.

</div>