# 🎣 Dual Fishing — Fabric Mod

> Позволяет удить рыбу двумя удочками одновременно!

---

## 📋 Описание

**Dual Fishing** — это мод для Minecraft на базе Fabric, который позволяет игроку держать удочки в обеих руках и забрасывать два поплавка одновременно. Поплавок в основной руке и поплавок во вспомогательной руке работают **независимо** друг от друга — каждый ловит рыбу самостоятельно, что вдвое увеличивает эффективность рыбалки.

---

## ✨ Возможности

- 🎣 **Два поплавка одновременно** — удочка в основной и во вспомогательной руке работают независимо.
- 🔁 **Независимое управление поплавками** — смотать одну удочку, не затрагивая другую.
- 🖥️ **Синхронизация клиент–сервер** — состояние поплавка вспомогательной руки передаётся по сети через `DataTracker`.
- 🎨 **Корректный рендер лески** — клиентский миксин исправляет отрисовку: леска от поплавка вспомогательной руки тянется к нужной руке персонажа.
- 🤝 **Совместимость** — мод не мешает стандартному поведению рыбалки, все ванильные механики (зачарования, опыт, дождь) сохранены.

---

## 🖱️ Управление

| Действие | Описание |
|---|---|
| ПКМ удочкой (основная рука) | Забросить / смотать поплавок основной руки |
| ПКМ удочкой (вспомогательная рука) | Забросить / смотать поплавок вспомогательной руки |
| Shift + ПКМ (основная рука) | Стандартное ванильное поведение (без двойной рыбалки) |

> **Важно:** чтобы использовать вспомогательную руку для рыбалки, в основной руке **тоже** должна быть удочка, иначе срабатывает стандартная логика Minecraft.

---

## 📦 Зависимости

| Зависимость | Версия |
|---|---|
| Minecraft | `>= 1.21.1 < 1.22` (протестировано на `1.21.11`) |
| Fabric Loader | `>= 0.16.2` |
| Java | `>= 21` |

> **Fabric API не требуется!** Мод работает только на Fabric Loader.

---

## ⚙️ Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/installer/) версии `0.16.2` или новее.
2. Скопируйте `.jar` файл мода в папку `%appdata%\.minecraft\mods`.
3. Запустите Minecraft с профилем Fabric.

---

## 🏗️ Сборка из исходников

### Требования

- **JDK 21** (включён в папку `jdk/` для удобства)
- **Gradle** (используется через Gradle Wrapper `gradlew.bat`)

### Шаги

```bash
# Сборка мода
.\gradlew.bat build

# Готовый JAR будет в:
# build/libs/dualfishing-1.0.0.jar
```

### Версии зависимостей (из `gradle.properties`)

```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.6
loader_version=0.16.2
mod_version=1.0.0
```

---

## 🧩 Техническая реализация

Мод использует систему **Mixin** от SpongePowered для изменения ванильного кода без перезаписи классов.

### Архитектура

```
com.dualfishing
├── DualFishingMod.java              — точка входа, регистрация TrackedData
├── DualFishingPlayer.java           — интерфейс, расширяющий PlayerEntity
└── mixin/
    ├── PlayerEntityMixin.java        — хранит поплавок вспомогательной руки,
    │                                   синхронизирует ID через DataTracker
    ├── FishingRodItemMixin.java      — перехватывает use() удочки,
    │                                   подменяет fishHook для вспомогательной руки
    ├── FishingBobberEntityMixin.java — очищает ссылку при удалении поплавка
    └── client/
        └── FishingBobberEntityRendererMixin.java — корректирует привязку
                                                     лески к нужной руке
```

### Ключевые решения

#### `DualFishingPlayer` — интерфейс-расширение

```java
public interface DualFishingPlayer {
    FishingBobberEntity getOffHandFishHook();
    void setOffHandFishHook(FishingBobberEntity fishHook);
    boolean isCastingOffhand();
    void setCastingOffhand(boolean castingOffhand);
}
```

Интерфейс внедряется в `PlayerEntity` через `PlayerEntityMixin`. Это позволяет безопасно кастовать любой `PlayerEntity` к `DualFishingPlayer` и получать доступ к поплавку вспомогательной руки.

#### Синхронизация через `TrackedData`

```java
public static final TrackedData<Integer> OFF_HAND_FISH_HOOK_ID =
    DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
```

ID поплавка вспомогательной руки синхронизируется с клиентом через `DataTracker`, что позволяет клиентскому рендереру (`FishingBobberEntityRendererMixin`) правильно определять, к какой руке крепить леску.

#### Подмена `fishHook` в `FishingRodItemMixin`

Ванильный метод `FishingRodItem.use()` обращается только к `player.fishHook` (поплавок основной руки). Чтобы удочка во вспомогательной руке управляла своим поплавком, миксин:

1. **До** выполнения `use()`: меняет местами `player.fishHook` и `offHandFishHook`.
2. **После** выполнения `use()`: возвращает значения обратно.

Таким образом, ванильный код «думает», что работает с основной рукой, а фактически управляет поплавком вспомогательной.

#### Клиентский рендер (`FishingBobberEntityRendererMixin`)

```java
@Redirect(method = "getHandPos", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/client/render/entity/FishingBobberEntityRenderer;getArmHoldingRod(...)"))
private Arm redirectGetArmHoldingRod(PlayerEntity player) {
    // Если этот поплавок — поплавок вспомогательной руки,
    // возвращаем противоположную руку
    int offHandId = player.getDataTracker().get(DualFishingMod.OFF_HAND_FISH_HOOK_ID);
    if (currentBobber.getId() == offHandId) {
        return player.getMainArm().getOpposite();
    }
    return FishingBobberEntityRenderer.getArmHoldingRod(player);
}
```

Используется `ThreadLocal<FishingBobberEntity>` для того, чтобы передать ссылку на текущий рендеримый поплавок в метод `getHandPos`, который не получает поплавок как аргумент.

---

## 📁 Структура проекта

```
FishingMine/
├── src/main/
│   ├── java/com/dualfishing/
│   │   ├── DualFishingMod.java
│   │   ├── DualFishingPlayer.java
│   │   └── mixin/
│   │       ├── FishingRodItemMixin.java
│   │       ├── FishingBobberEntityMixin.java
│   │       ├── PlayerEntityMixin.java
│   │       └── client/
│   │           └── FishingBobberEntityRendererMixin.java
│   └── resources/
│       ├── fabric.mod.json
│       ├── dualfishing.mixins.json
│       └── dualfishing.client.mixins.json
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## 📄 Лицензия

**CC0-1.0** — передано в общественное достояние. Используйте, модифицируйте и распространяйте свободно без каких-либо ограничений.


# ⚒️ Uncrafting Table — Fabric Mod

> Разбирай предметы обратно на составляющие прямо в верстаке!

---

## 📋 Описание

**Uncrafting Table** — это мод для Minecraft на базе Fabric, который превращает обычный верстак в **стол разборки**. Просто положи готовый предмет в слот результата верстака — и мод автоматически отобразит рецепт его создания, разложив ингредиенты по сетке 3×3. Забери ингредиенты — и предмет разобран.

Никаких новых блоков, никакого нового UI — всё работает через стандартный интерфейс верстака.

---

## ✨ Возможности

- ♻️ **Разборка любого крафтового предмета** — работает со всеми shaped и shapeless рецептами.
- 🔲 **Родной UI** — используется стандартный верстак, никаких новых блоков или меню.
- 📐 **Корректная сетка** — для shaped рецептов ингредиенты расставляются точно по той же схеме, что и при крафте.
- 🔢 **Умная обработка стопок** — если предмет создаётся в количестве > 1 (например, 4 доски из 1 бревна), вы можете положить ровно столько, сколько нужно; лишнее автоматически возвращается в инвентарь.
- ⚡ **Без лагов** — поиск рецепта выполняется только при размещении предмета, не при каждом тике.

---

## 🖱️ Как пользоваться

1. Откройте **верстак** (обычный, ванильный).
2. Убедитесь, что **сетка 3×3 пуста**.
3. Возьмите **крафтовый предмет** в курсор (ЛКМ по предмету в инвентаре).
4. Кликните **ЛКМ по слоту результата** (слот 0) верстака.
5. Предмет окажется в слоте результата, а сетка заполнится ингредиентами его рецепта.
6. Заберите ингредиенты из сетки.

> **Ограничение:** работает только если сетка верстака **полностью пуста** перед размещением предмета.

---

## 📦 Зависимости

| Зависимость | Версия |
|---|---|
| Minecraft | `>= 1.21.11 < 1.22` |
| Fabric Loader | `>= 0.16.2` |
| Java | `>= 21` |

> **Fabric API не требуется!** Мод работает только на Fabric Loader.

---

## ⚙️ Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/installer/) версии `0.16.2` или новее.
2. Скопируйте `.jar` файл мода в папку `%appdata%\.minecraft\mods`.
3. Запустите Minecraft с профилем Fabric.

---

## 🏗️ Сборка из исходников

### Требования

- **JDK 21** (включён в папку `jdk/` для удобства)
- **Gradle** (используется через Gradle Wrapper `gradlew.bat`)

### Шаги

```bash
# Сборка мода
.\gradlew.bat build

# Готовый JAR будет в:
# build/libs/uncrafting-1.0.0.jar
```

### Версии зависимостей (из `gradle.properties`)

```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.6
loader_version=0.16.2
mod_version=1.0.0
```

---

## 🧩 Техническая реализация

Мод использует систему **Mixin** от SpongePowered и **Access Widener** для доступа к приватным полям ванильных классов.

### Архитектура

```
com.uncrafting
├── UncraftingMod.java               — точка входа
├── UncraftingHelper.java            — логика поиска рецепта и разборки
└── mixin/
    ├── ScreenHandlerMixin.java       — перехватывает клик по слоту результата,
    │                                   запускает разборку до и после vanilla-клика
    ├── CraftingScreenHandlerMixin.java — блокирует onContentChanged во время
    │                                   разборки, чтобы верстак не сбросил слот 0
    └── CraftingResultSlotMixin.java  — разрешает вставку предмета в слот результата
```

### Access Widener

Для доступа к полю `result` классов `ShapedRecipe` и `ShapelessRecipe` (package-private в 1.21.11) используется Access Widener:

```
accessWidener v2 named

accessible field net/minecraft/recipe/ShapedRecipe result Lnet/minecraft/item/ItemStack;
accessible field net/minecraft/recipe/ShapelessRecipe result Lnet/minecraft/item/ItemStack;
```

### Ключевые решения

#### Проблема: ванильный `onContentChanged` сбрасывает слот 0

Когда игрок кладёт предмет в верстак, `CraftingScreenHandler.onContentChanged()` немедленно вызывается и пересчитывает рецепт, очищая слот результата. Это мешает нашей логике, которая должна найти рецепт **после** того, как предмет окажется в слоте 0.

**Решение:** флаг `ThreadLocal<Boolean> IS_UNCRAFTING`.

1. `ScreenHandlerMixin.beforeSlotClick()` — устанавливает флаг в `true` **до** клика.
2. `CraftingScreenHandlerMixin.onContentChanged()` — отменяет выполнение метода, пока флаг `true`.
3. `ScreenHandlerMixin.afterSlotClick()` — после клика читает предмет из слота 0, ищет рецепт, заполняет сетку, сбрасывает флаг.

```java
// ThreadLocal гарантирует потокобезопасность (хотя Minecraft однопоточный на сервере)
public static final ThreadLocal<Boolean> IS_UNCRAFTING = ThreadLocal.withInitial(() -> false);
```

#### `CraftingResultSlotMixin` — разрешение вставки

По умолчанию слот результата верстака (`CraftingResultSlot`) запрещает вставку предметов. Миксин переопределяет это поведение:

```java
@Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
private void onCanInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    cir.setReturnValue(true); // Разрешаем вставку всегда
}
```

#### `UncraftingHelper.findRecipeFor()` — поиск рецепта

Метод перебирает все рецепты через `ServerRecipeManager.values()` и ищет первый `CraftingRecipe`, чей выход совпадает с переданным предметом.

- Для **ShapedRecipe**: ингредиенты маппируются из сетки `width × height` в сетку `3 × 3`.
- Для **ShapelessRecipe**: ингредиенты расставляются линейно, начиная с ячейки 0.
- Для преобразования `Ingredient` → `ItemStack` используется `Ingredient.getMatchingItems()` (API 1.21.11).

```java
// Пример маппинга shaped рецепта на 3x3 сетку
for (int row = 0; row < height; row++) {
    for (int col = 0; col < width; col++) {
        int ingredientIndex = row * width + col;
        int gridIndex = row * 3 + col; // позиция в 3x3 сетке
        // ...
    }
}
```

#### Обработка стопок

Если рецепт создаёт предмет в количестве > 1 (например, `Stick` из `Planks` даёт 4 палки), а игрок кладёт больше нужного, лишние предметы возвращаются в инвентарь:

```java
if (outputStack.getCount() > recipeOutput.getCount()) {
    ItemStack excess = outputStack.copy();
    excess.setCount(outputStack.getCount() - recipeOutput.getCount());
    if (!player.getInventory().insertStack(excess)) {
        player.dropStack(serverWorld, excess); // на землю, если инвентарь полон
    }
}
```

---

## 📁 Структура проекта

```
UncraftingMod/
├── src/main/
│   ├── java/com/uncrafting/
│   │   ├── UncraftingMod.java
│   │   ├── UncraftingHelper.java
│   │   └── mixin/
│   │       ├── ScreenHandlerMixin.java
│   │       ├── CraftingScreenHandlerMixin.java
│   │       └── CraftingResultSlotMixin.java
│   └── resources/
│       ├── fabric.mod.json
│       ├── uncrafting.mixins.json
│       └── uncrafting.accesswidener
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## ⚠️ Известные ограничения

- Работает только с рецептами типа **Shaped** и **Shapeless**. Специальные рецепты (переплавка, варка зелий, наковальня) не поддерживаются.
- Для разборки сетка верстака должна быть **пустой**. Если в сетке уже есть предметы, разборка не произойдёт.
- Мод использует **первый найденный рецепт** для данного предмета. Если у предмета несколько рецептов — всегда берётся один (порядок определяется итерацией рецептов в `RecipeManager`).
- Работает только **на сервере** (включая одиночную игру), так как требует доступа к `ServerWorld` и `ServerRecipeManager`.

---

## 📄 Лицензия

**CC0-1.0** — передано в общественное достояние. Используйте, модифицируйте и распространяйте свободно без каких-либо ограничений.
