# DSL Examples
## Modelling simple flow rules
Modelling a constraint matching flows from an originating node that has data properties A to a vertex with vertex properties B:
```
data A neverFlows vertex B
```

::: tip Examples 
Sensitive Data never flows to a server outside of the EU:
```
data Type.Sensitive neverFlows vertex Location.nonEU
```

Internal Data never flows to the user:
```
data Type.Internal neverFlows vertex Role.User
```
:::

## Modelling Access Control 
Modelling a constraint matching access control rules for RequiredRoles and AssignedRoles:
```
data AssignedRoles.$Assigned 
neverFlows 
vertex RequiredRoles.$Required
where
present $Assigned
present $Required
empty intersection($Assigned,$Required)
```
