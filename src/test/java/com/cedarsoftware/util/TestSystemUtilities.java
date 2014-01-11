package com.cedarsoftware.util;

import com.cedarsoftware.test.Asserter;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) Cedar Software LLC
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class TestSystemUtilities
{
    @Test
    public void testConstructor() {
        Asserter.assertClassOnlyHasAPrivateDefaultConstructor(SystemUtilities.class);

    }

    @Test
    public void testGetExternalVariable()
    {
        Assert.assertNotNull(SystemUtilities.getExternalVariable("PATH"));
        Assert.assertNotNull(SystemUtilities.getExternalVariable("Path"));

        Assert.assertNull(SystemUtilities.getExternalVariable("foo"));

        Assert.assertNull(System.getenv("java.vm.version"));
        Assert.assertNotNull(SystemUtilities.getExternalVariable("java.vm.version"));
        Assert.assertNotNull(SystemUtilities.getExternalVariable("java.vm.vendor"));

        //System.out.println(System.getProperties().size());
        //Enumeration e = System.getProperties().keys();
        //while (e.hasMoreElements()) {
        //    System.out.println(e.nextElement());
        //}
        //Assert.assertNotNull(SystemUtilities.getExternalVariable(""));
    }
}
