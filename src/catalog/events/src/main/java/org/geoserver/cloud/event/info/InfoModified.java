/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event.info;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.geoserver.catalog.plugin.Patch;
import org.geoserver.cloud.event.catalog.CatalogInfoModified;
import org.geoserver.cloud.event.config.ConfigInfoModified;
import org.springframework.core.style.ToStringCreator;

import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CatalogInfoModified.class),
    @JsonSubTypes.Type(value = ConfigInfoModified.class),
})
@SuppressWarnings("serial")
public abstract class InfoModified extends InfoEvent {

    private @Getter @Setter Patch patch;

    protected InfoModified() {}

    protected InfoModified(
            long updateSequence,
            @NonNull String objectId,
            @NonNull ConfigInfoType objectType,
            @NonNull Patch patch) {
        super(updateSequence, objectId, objectType);
        this.patch = patch;
    }

    protected @Override ToStringCreator toStringBuilder() {
        return super.toStringBuilder()
                .append(
                        "changes",
                        getPatch().getPropertyNames().stream().collect(Collectors.joining(",")));
    }
}
