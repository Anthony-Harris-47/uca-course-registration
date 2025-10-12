# Changelog – UCA Course Registration System Refactor
### Version 1.0.0 [10/12/2025]

## Added
- **Layered architecture**:
    - **Model layer** 
    - **Repository layer** 
    - **Service layer** 
    - **Application layer**
- Dependency injection to manage components and remove global state.
- Reusable utilities for configuration and logging.
- Modular project structure with 14 files organized across 5 packages.
- Separation of responsibilities, independent testing for each layer.
- New persistence types can be added without modifying code.


## Changed
- Altered monolithic design into a layered architecture.
- Pulled file I/O logic out of model classes and into repository implementations.
- Main class now uses dependency injection instead of static state.
- Replaced mixed UI, persistence, and domain logic with isolated responsibilities.
- Created consistent logging across the whole program.
- Hardcoded dependencies now replaced by interfaces.

## Removed
- Global static state.
- Monolithic structure.
- CSV I/O that was tightly coupled directly with the model and service code.
- Mixed UI, domain, and persistence logic.



## Result
The project now implements SOLID design principles.
Components operate independently, which allows for easy extension and modification of the 
codebase, without impacting other layers.