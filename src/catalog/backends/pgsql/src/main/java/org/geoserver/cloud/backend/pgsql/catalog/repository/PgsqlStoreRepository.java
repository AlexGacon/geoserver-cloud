/*
 * (c) 2023 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.backend.pgsql.catalog.repository;

import lombok.NonNull;

import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.CatalogInfoRepository.StoreRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @since 1.4
 */
public class PgsqlStoreRepository extends PgsqlCatalogInfoRepository<StoreInfo>
        implements StoreRepository {

    /**
     * @param template
     */
    public PgsqlStoreRepository(@NonNull JdbcTemplate template) {
        super(template);
    }

    @Override
    public Class<StoreInfo> getContentType() {
        return StoreInfo.class;
    }

    @Override
    protected String getQueryTable() {
        return "storeinfos";
    }

    @Override
    public <U extends StoreInfo> Optional<U> findById(@NonNull String id, Class<U> clazz) {
        String sql =
                """
                SELECT store, workspace
                FROM storeinfos
                WHERE id = ?
                """;
        return findOne(sql, clazz, newRowMapper(), id);
    }

    @Override
    public void setDefaultDataStore(
            @NonNull WorkspaceInfo workspace, @NonNull DataStoreInfo dataStore) {
        String sql = "UPDATE workspaceinfo SET default_store = ? WHERE id = ?";
        template.update(sql, dataStore.getId(), workspace.getId());
    }

    @Override
    public void unsetDefaultDataStore(@NonNull WorkspaceInfo workspace) {
        String sql = "UPDATE workspaceinfo SET default_store = NULL WHERE id = ?";
        template.update(sql, workspace.getId());
    }

    @Override
    public Optional<DataStoreInfo> getDefaultDataStore(@NonNull WorkspaceInfo workspace) {
        String sql =
                """
                SELECT store, workspace
                FROM storeinfos
                WHERE "workspace.id" = ? AND default_store IS NOT NULL
                """;
        return findOne(sql, DataStoreInfo.class, CatalogInfoRowMapper.store(), workspace.getId());
    }

    @Override
    public Stream<DataStoreInfo> getDefaultDataStores() {
        String sql =
                """
                SELECT store, workspace
                FROM storeinfos
                WHERE default_store IS NOT NULL
                """;
        return template.queryForStream(sql, CatalogInfoRowMapper.store())
                .filter(DataStoreInfo.class::isInstance)
                .map(DataStoreInfo.class::cast);
    }

    @Override
    public <T extends StoreInfo> Stream<T> findAllByWorkspace(
            @NonNull WorkspaceInfo workspace, @NonNull Class<T> clazz) {

        String sql =
                """
                SELECT store, workspace
                FROM storeinfos
                WHERE "workspace.id" = ?
                """;

        Stream<StoreInfo> stores;
        String workspaceId = workspace.getId();
        if (StoreInfo.class.equals(clazz)) {
            stores = template.queryForStream(sql, CatalogInfoRowMapper.store(), workspaceId);
        } else {
            String infotype = infoType(clazz);
            sql += " AND \"@type\" = ?::infotype";
            stores =
                    template.queryForStream(
                            sql, CatalogInfoRowMapper.store(), workspaceId, infotype);
        }

        return stores.filter(clazz::isInstance).map(clazz::cast);
    }

    @Override
    public <T extends StoreInfo> Stream<T> findAllByType(@NonNull Class<T> clazz) {
        RowMapper<StoreInfo> rowMapper = CatalogInfoRowMapper.store();
        Stream<StoreInfo> stores;

        if (StoreInfo.class.equals(clazz)) {
            stores = template.queryForStream("SELECT store, workspace FROM storeinfos", rowMapper);
        } else {
            String type = clazz.getSimpleName();
            stores =
                    template.queryForStream(
                            "SELECT store, workspace FROM storeinfos WHERE \"@type\" = ?::infotype",
                            rowMapper,
                            type);
        }

        return stores.filter(clazz::isInstance).map(clazz::cast);
    }

    @Override
    public <T extends StoreInfo> Optional<T> findByNameAndWorkspace(
            @NonNull String name, @NonNull WorkspaceInfo workspace, @NonNull Class<T> clazz) {

        return findOne(
                """
                SELECT store, workspace FROM storeinfos WHERE "workspace.id" = ? AND name = ?
                """,
                clazz,
                workspace.getId(),
                name);
    }

    @Override
    protected RowMapper<StoreInfo> newRowMapper() {
        return CatalogInfoRowMapper.store();
    }
}
