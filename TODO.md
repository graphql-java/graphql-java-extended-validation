

# add the ability to do inter argument validation

eg imagine

    field( first : Int, after : ID, last : Int, before : ID)
    
can we do a validation rule where we require `first`+`after` or `last` + `before` but not all 4 together??

Currently the rules are single argument specific - we would need field specific rules

# naming

Since we take inspiration from javax validation perhaps we should use
the name Constraint especially for the @directives rules

# i18n
 
 We rally should look at using the Hibernate message interpolation code
 (by building a bridge between our own) and get I18n and EL in messages
 working for near free
 
 https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#preface
 
 
 https://github.com/hibernate/hibernate-validator
 
 # Allowing Java EL
 
 Can we allow Java EL so that we can have expressions in rules
 
 ```
    ${validatedValue.length()} > 50
``` 
 
 Does this makes sense?  What can we do to make this more powerful?
 
 Hibernate Validator uses a form of this to do message interpolation
 