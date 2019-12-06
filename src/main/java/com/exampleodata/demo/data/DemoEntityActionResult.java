package com.exampleodata.demo.data;

import org.apache.olingo.commons.api.data.Entity;

public class DemoEntityActionResult {
    private Entity entity;
    private boolean created = false;

    public Entity getEntity() {
        return entity;
    }

    public DemoEntityActionResult setEntity(final Entity entity) {
        this.entity = entity;
        return this;
    }

    public boolean isCreated() {
        return created;
    }

    public DemoEntityActionResult setCreated(final boolean created) {
        this.created = created;
        return this;
    }


}
