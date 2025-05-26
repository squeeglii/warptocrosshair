package me.cg360.mod.warptocrosshair.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import me.cg360.mod.warptocrosshair.WarpToCrosshairMod;
import me.cg360.mod.warptocrosshair.config.helper.*;
import me.cg360.mod.warptocrosshair.util.ReflectSupport;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class ConfigUIBuilder {

    public static DecimalFormat FLOAT_FORMAT = new DecimalFormat("#.##");

    public static String DEFAULT_CATEGORY_NAME = "other".trim().toLowerCase(); // enforce lowercase.


    public static YetAnotherConfigLib buildConfig() {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder();
        Map<String, List<Field>> sortedCategories = sortConfigIntoCategories(WTCConfig.class);

        //builder.category(createPresetsCategory()); -- will consider again. Couldn't make UI update fully.

        for(String categoryName : sortedCategories.keySet()) {
            List<Field> categoryOptions = sortedCategories.get(categoryName);
            ConfigCategory category = createCategory(categoryName, categoryOptions);

            builder.category(category);
        }

        return builder
                .title(Component.translatable(ConfigUtil.TRANSLATION_TITLE))
                .save(WTCConfig.HANDLER::save)
                //.screenInit(screen -> ...)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<Option<T>> createOption(Field field, Function<Option<T>, ControllerBuilder<T>> controllerBuilder) {
        String id = field.getName();

        String nameTranslation = ConfigUtil.TRANSLATION_OPTION_NAME.formatted(id);

        WTCConfig instance = WTCConfig.HANDLER.instance();
        Object defaultValue = instance.getDefaultForField(field).orElse(null);

        if(defaultValue == null) {
            WarpToCrosshairMod.getLogger().error("Config field '{}' has no default value. Skipping.", id);
            return Optional.empty();
        }

        T castedDefault = (T) defaultValue; // trust.

        Option.Builder<T> option = Option.<T>createBuilder()
                .name(Component.translatable(nameTranslation))
                .binding(castedDefault,
                        () -> {
                            try {
                                field.setAccessible(true);
                                Object val = field.get(instance);
                                return val == null
                                        ? castedDefault
                                        : (T) val;
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        val -> {
                            try {
                                field.setAccessible(true);
                                field.set(instance, val);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .controller(controllerBuilder);

        IncludeExtraDescription[] descriptionNotation = field.getDeclaredAnnotationsByType(IncludeExtraDescription.class);
        IncludeImage[] imageNotation = field.getDeclaredAnnotationsByType(IncludeImage.class);
        IncludeAnimatedImage[] animatedImageNotation = field.getDeclaredAnnotationsByType(IncludeAnimatedImage.class);
        OptionDescription.Builder desc = OptionDescription.createBuilder();

        // Everything has a default description [config.bridgingmod.option.[field].description.0
        // If key isn't defined in translations, it's just blank.
        String descTranslationKey = ConfigUtil.TRANSLATION_OPTION_DESCRIPTION.formatted(id, 0);
        Component descTranslation = Component.translatableWithFallback(descTranslationKey, "");
        desc.text(descTranslation);

        // IncludeExtraDescriptions present
        // If present, [i] extra lines are added for key [config.bridgingmod.option.[field].description.[i],
        // WITHOUT a fallback.
        if(descriptionNotation.length > 0) {
            int extraParagraphs = Math.max(descriptionNotation[0].extraParagraphs(), 1); // must be >1, else snap to 1.

            for(int i = 1; i < extraParagraphs + 1; i++) {
                String extraDescriptionTranslationKey = ConfigUtil.TRANSLATION_OPTION_DESCRIPTION.formatted(id, i);
                Component extraDescTranslation = Component.translatable(extraDescriptionTranslationKey);
                desc.text(extraDescTranslation);
            }
        }

        // IncludeImage present
        if(imageNotation.length > 0) {
            IncludeImage imageAnnotation = imageNotation[0];
            ResourceLocation checkedPath = WarpToCrosshairMod.id(imageAnnotation.value());
            int width = imageAnnotation.width();
            int height = imageAnnotation.height();
            desc.image(checkedPath, width, height);
        }

        if(animatedImageNotation.length > 0) {
            IncludeAnimatedImage imageAnnotation = animatedImageNotation[0];
            ResourceLocation checkedPath = WarpToCrosshairMod.id(imageAnnotation.value());
            desc.webpImage(checkedPath);
        }

        return Optional.of(option.description(desc.build()).build());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ConfigCategory createCategory(String categoryName, List<Field> categoryOptions) {
        ConfigCategory.Builder category = ConfigCategory.createBuilder();
        String translatedName = ConfigUtil.TRANSLATION_CATEGORY_NAME.formatted(categoryName);
        String translatedTooltip = ConfigUtil.TRANSLATION_CATEGORY_TOOLTIP.formatted(categoryName);
        category.name(Component.translatable(translatedName));
        category.tooltip(Component.translatableWithFallback(translatedTooltip, ""));

        for(Field field : categoryOptions) {

            // If it's a primitive, box it in its object type to make isAssignableFrom more reliable.
            Class<?> type = ReflectSupport.boxPrimitive(field.getType());

            if(Boolean.class.isAssignableFrom(type)) {
                Optional<Option<Boolean>> optOption = createOption(field, TickBoxControllerBuilder::create);
                optOption.ifPresent(category::option);
                continue;
            }

            if(Integer.class.isAssignableFrom(type)) {
                DiscreteRange[] discreteRangeAnno = field.getDeclaredAnnotationsByType(DiscreteRange.class);

                if(discreteRangeAnno.length > 0) {
                    DiscreteRange range = discreteRangeAnno[0];

                    ValueFormatter<Integer> valFormatter = range.formatTranslationKey().isEmpty()
                            ? val -> Component.literal(val.toString())
                            : val -> Component.translatable(range.formatTranslationKey(), val.toString());

                    Optional<Option<Integer>> optOption = createOption(
                            field,
                            option -> IntegerSliderControllerBuilder.create(option)
                                    .range(range.min(), range.max())
                                    .step(1)
                                    .formatValue(valFormatter)
                    );

                    optOption.ifPresent(category::option);
                    continue;
                }

                Optional<Option<Integer>> optOption = createOption(field, IntegerFieldControllerBuilder::create);
                optOption.ifPresent(category::option);
                continue;
            }

            if(Float.class.isAssignableFrom(type)) {
                ContinuousRange[] discreteRangeAnno = field.getDeclaredAnnotationsByType(ContinuousRange.class);

                if(discreteRangeAnno.length > 0) {
                    ContinuousRange range = discreteRangeAnno[0];

                    ValueFormatter<Float> valFormatter = range.formatTranslationKey().isEmpty()
                            ? val -> Component.literal(FLOAT_FORMAT.format(val))
                            : val -> Component.translatable(range.formatTranslationKey(), FLOAT_FORMAT.format(val));

                    Optional<Option<Float>> optOption = createOption(
                            field,
                            option -> FloatSliderControllerBuilder.create(option)
                                    .range(range.min(), range.max())
                                    .step(range.sliderStep())
                                    .formatValue(valFormatter)
                    );

                    optOption.ifPresent(category::option);
                    continue;
                }

                Optional<Option<Float>> optOption = createOption(field, FloatFieldControllerBuilder::create);
                optOption.ifPresent(category::option);
                continue;
            }

            if(Color.class.isAssignableFrom(type)) {
                Optional<Option<Color>> optOption = createOption(field, opt -> ColorControllerBuilder.create(opt).allowAlpha(true));
                optOption.ifPresent(category::option);
                continue;
            }

            if(Enum.class.isAssignableFrom(type)) {
                // EnumControllerBuilder use literal names by default for the button label.
                // This is still the default, unless the enum extends Translatable & provides
                // translation keys for each name.
                Optional<Option<Enum>> optOption = createOption(
                        field,
                        opt -> EnumControllerBuilder
                                .create(opt)
                                .enumClass((Class<Enum>) type)
                                .formatValue(val ->
                                        val instanceof Translatable translatable
                                                ? Component.translatable(translatable.getTranslationKey())
                                                : Component.literal(((Enum) val).name()) // Causes build error without a cast. Keep it, even with IDE warning.
                                )
                );
                optOption.ifPresent(category::option);
                continue;
            }

            // [ new types here ]
            WarpToCrosshairMod.getLogger().warn("Skipped displaying config entry '{}' as its type has no display logic.", field.getName());

        }

        return category.build();
    }

    /** Get all the valid config fields in BridgingConfig*/
    private static Map<String, List<Field>> sortConfigIntoCategories(Class<?> configClass) {
        Field[] fields = configClass.getDeclaredFields();
        Map<String, List<Field>> sortedCategories = new LinkedHashMap<>(fields.length);

        for(Field field : fields) {

            // check if field is actually a config entry that should be visible
            int modifiers = field.getModifiers();

            if(Modifier.isFinal(modifiers)) continue;
            if(Modifier.isStatic(modifiers)) continue;
            if(field.getDeclaredAnnotationsByType(HideInConfigUI.class).length > 0) continue;

            // Now we do some sorting!
            Category[] foundCategories = field.getDeclaredAnnotationsByType(Category.class);

            // If a category tag is found sort the field into that category (or use the default name if value is null)
            // Else just chuck it into the default category.
            // Categories are only really used for tabs.
            String categoryName;

            if(foundCategories.length > 0) {
                String firstCatName = foundCategories[0].value();
                categoryName = firstCatName == null
                        ? DEFAULT_CATEGORY_NAME
                        : firstCatName.trim().toLowerCase();
            } else {
                categoryName = DEFAULT_CATEGORY_NAME;
            }

            // If this is the first time the category has came up, make a new list to sort into.
            if(!sortedCategories.containsKey(categoryName)) {
                sortedCategories.put(categoryName, new LinkedList<>());
            }

            sortedCategories.get(categoryName).add(field);
        }

        return sortedCategories;
    }

}
