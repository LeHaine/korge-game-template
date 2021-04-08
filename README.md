A base game template project for getting Korge games up and running quickly.

## How to use

Extend the `Entity` class to create new entities that use the grid positioning logic.

To create new components extend the `Component` interface or one of many other existing interfaces such as
* `DynamicComponent` - A simple physics movement with no checks to collision. Override the collision checks to add your own
* `PlatformerDynamicComponent` - A platformer dynamic component that handles collision and gravity for platformers
* `SpriteComponent` - This is a `DrawableComponent` and a `StretchAndScaleComponent` in one that allows stretching and scaling of a `EnhancedSprite`.

And many others.
  
Use these components to create new entities by using composition vs inheritance.
Mix and match to prevent coupling of components and use the new entity as the behavior.

Example:
```kotlin
class Hero(
    private val platformerDynamic: PlatformerDynamicComponent,
    private val spriteComponent: SpriteComponent,
    level: GenericGameLevelComponent<LevelMark>,
    container: Container
) : Entity(level, container),
    PlatformerDynamicComponent by platformerDynamic,
    SpriteComponent by spriteComponent {
        // add logic here
    }
```
