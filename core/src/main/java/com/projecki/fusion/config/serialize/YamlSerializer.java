package com.projecki.fusion.config.serialize;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Optional;


/**
 * @deprecated No longer in use, in favor of {@link JacksonSerializer<T>}
 *
 * @param <T> the class this serializer is used for
 */
@Deprecated(forRemoval = true)
public class YamlSerializer<T> extends Serializer<T> {

    public YamlSerializer(Class<T> targetType) {
        super(targetType);
    }

    @Override
    public String serialize(T object) {
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.dumpAs(object, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
    }

    @Override
    public Optional<T> deserialize(String s) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(super.targetType.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        T object = yaml.loadAs(s, super.targetType);
        return Optional.ofNullable(object);
    }

    @Override
    public String getExtension() {
        return "yml";
    }
}
