# Palladio Component Model

Palladio is a modeling and analysis approach for component-based software architectures. The modeling is done in collaboration of multiple roles:

## Models
The Palladio approach distinguishes five roles. Four of these roles model system aspects that are required to capture a holistic view on the overall software architecture. The following figure illustrates these four roles and the models they create.
<img src="/img/palladio-roles-and-models.svg" alt="Roles and models describing a full software architecture according to Palladio"/>
The following sections briefly describe the models and the roles that create these models.

### Repository Model
The repository model contains components, interfaces and data types that component developers create. These elements are the building blocks for systems.
A component always has a clearly defined interface, through which all interactions have to happen. Consequently, the provided services are described through provided interfaces and the required services are described through required interfaces. Because of this clearly defined interface, components can be instantiated and wired as part of systems.
For every provided service, the components contain a description of how this service is provided within a so called service effect specification (SEFF). The information contained in a SEFF is usually tailored to the particular quality that shall be analyzed.

### System / Assembly Model
An assembly model consists of a set of wired component instances that provide and require a defined set of services. The component instances are instances of the components specified in the repository model.
Assembly models can be created by component developers and software architects. If component developers create the assembly model, they typically describe composite components, i.e. components that are made of multiple other components, and place that composite component in the repository model. Software architects typically create a system model, which is just another name for an assembly model, which describes a whole system. A system model is mandatory. Assembly models describing composite components might be used to structure the repository model.

### Resource Environment
A resource environment describes the resources that can be used to deploy the system parts. The resources are essentially computing resources that are connected via network links. Typically the system deployer creates this environment.

### Allocation Model
The allocation model describes how component instances used in the system model are allocated, i.e. deployed, to resources from the resource environment. The system deployer creates this model.

### Usage Model
The usage model describes how different types of users use the system. A domain expert creates this model by specifying a sequence of usages of system services by user groups.
