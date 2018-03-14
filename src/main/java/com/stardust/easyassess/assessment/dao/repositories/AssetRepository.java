package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.Asset;

public interface AssetRepository extends DataRepository<Asset, String> {
    default Class<Asset> getEntityClass() {
        return Asset.class;
    }
}
