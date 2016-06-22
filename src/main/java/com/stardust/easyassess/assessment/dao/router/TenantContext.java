package com.stardust.easyassess.assessment.dao.router;


public class TenantContext {

    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantDB) {
        currentTenant.set(tenantDB);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }
}