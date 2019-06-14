

# add the ability to do inter argument validation

eg imagine

    field( first : Int, after : ID, last : Int, before : ID)
    
can we do a validation rule where we require `first`+`after` or `last` + `before` but not all 4 together??

Currently the rules are single argument specific - we would need field specific rules

