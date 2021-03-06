package io.gumga.domain.test.money;

import io.gumga.core.GumgaThreadScope;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GumgaSharedModelUUIDTest {

    @Test
    public void verifyOi() {
        GumgaThreadScope.organizationCode.set("1.");
        EntityGumgaSharedModelUUID testEntityV2 = new EntityGumgaSharedModelUUID();
        Assert.assertEquals("1.", testEntityV2.getOi().getValue());
    }
}
