package org.apache.archiva.policies;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.test.utils.ArchivaSpringJUnit4ClassRunner;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * ChecksumPolicyTest
 *
 *
 */
@RunWith( ArchivaSpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml"} )
public class ChecksumPolicyTest
{
    private static final String GOOD = "good";

    private static final String BAD = "bad";

    @Inject
    @Named( value = "postDownloadPolicy#checksum" )
    PostDownloadPolicy downloadPolicy;

    @Rule
    public TestName name = new TestName();

    private PostDownloadPolicy lookupPolicy()
        throws Exception
    {
        return downloadPolicy;
    }

    @Test
    public void testFailOnFileOnly()
        throws Exception
    {
        assertFailSetting( false, null, null );
    }

    @Test
    public void testFailOnFileWithBadMd5AndBadSha1()
        throws Exception
    {
        assertFailSetting( false, BAD, BAD );
    }

    @Test
    public void testFailOnFileWithBadMd5AndGoodSha1()
        throws Exception
    {
        assertFailSetting( false, BAD, GOOD );
    }

    @Test
    public void testFailOnFileWithBadMd5Only()
        throws Exception
    {
        assertFailSetting( false, BAD, null );
    }

    @Test
    public void testFailOnFileWithBadSha1Only()
        throws Exception
    {
        assertFailSetting( false, null, BAD );
    }

    @Test
    public void testFailOnFileWithGoodMd5AndBadSha1()
        throws Exception
    {
        assertFailSetting( false, GOOD, BAD );
    }

    @Test
    public void testFailOnFileWithGoodMd5AndGoodSha1()
        throws Exception
    {
        assertFailSetting( true, GOOD, GOOD );
    }

    @Test
    public void testFailOnFileWithGoodMd5Only()
        throws Exception
    {
        assertFailSetting( true, GOOD, null );
    }

    @Test
    public void testFailOnFileWithGoodSha1Only()
        throws Exception
    {
        assertFailSetting( true, null, GOOD );
    }

    @Test
    public void testFixOnFileOnly()
        throws Exception
    {
        assertFixSetting( true, null, null );
    }

    @Test
    public void testFixOnFileWithBadMd5AndBadSha1()
        throws Exception
    {
        assertFixSetting( true, BAD, BAD );
    }

    @Test
    public void testFixOnFileWithBadMd5AndGoodSha1()
        throws Exception
    {
        assertFixSetting( true, BAD, GOOD );
    }

    @Test
    public void testFixOnFileWithBadMd5Only()
        throws Exception
    {
        assertFixSetting( true, BAD, null );
    }

    @Test
    public void testFixOnFileWithBadSha1Only()
        throws Exception
    {
        assertFixSetting( true, null, BAD );
    }

    @Test
    public void testFixOnFileWithGoodMd5AndBadSha1()
        throws Exception
    {
        assertFixSetting( true, GOOD, BAD );
    }

    @Test
    public void testFixOnFileWithGoodMd5AndGoodSha1()
        throws Exception
    {
        assertFixSetting( true, GOOD, GOOD );
    }

    @Test
    public void testFixOnFileWithGoodMd5Only()
        throws Exception
    {
        assertFixSetting( true, GOOD, null );
    }

    @Test
    public void testFixOnFileWithGoodSha1Only()
        throws Exception
    {
        assertFixSetting( true, null, GOOD );
    }

    @Test
    public void testIgnore()
        throws Exception
    {
        PostDownloadPolicy policy = lookupPolicy();
        Path localFile = createTestableFiles( null, null );
        Properties request = createRequest();

        policy.applyPolicy( ChecksumPolicy.IGNORE, request, localFile );
    }

    private void assertFailSetting( boolean expectedResult, String md5State, String sha1State )
        throws Exception
    {
        PostDownloadPolicy policy = lookupPolicy();
        Path localFile = createTestableFiles( md5State, sha1State );
        Properties request = createRequest();

        boolean actualResult;

        try
        {
            policy.applyPolicy( ChecksumPolicy.FAIL, request, localFile );
            actualResult = true;
        }
        catch ( PolicyViolationException e )
        {
            actualResult = false;
            String msg = createMessage( ChecksumPolicy.FAIL, md5State, sha1State );

            assertFalse( msg + " local file should not exist:", Files.exists(localFile) );
            Path md5File = localFile.toAbsolutePath().resolveSibling( localFile.getFileName() + ".sha1" );
            Path sha1File = localFile.toAbsolutePath().resolveSibling( localFile.getFileName() + ".md5" );
            assertFalse( msg + " local md5 file should not exist:", Files.exists(md5File) );
            assertFalse( msg + " local sha1 file should not exist:", Files.exists(sha1File) );
        }

        assertEquals( createMessage( ChecksumPolicy.FAIL, md5State, sha1State ), expectedResult, actualResult );
    }

    private void assertFixSetting( boolean expectedResult, String md5State, String sha1State )
        throws Exception
    {
        PostDownloadPolicy policy = lookupPolicy();
        Path localFile = createTestableFiles( md5State, sha1State );
        Properties request = createRequest();

        boolean actualResult;

        try
        {
            policy.applyPolicy( ChecksumPolicy.FIX, request, localFile );
            actualResult = true;
        }
        catch ( PolicyViolationException e )
        {
            actualResult = false;
        }

        assertEquals( createMessage( ChecksumPolicy.FIX, md5State, sha1State ), expectedResult, actualResult );

        // End result should be legitimate SHA1 and MD5 files.
        Path md5File = localFile.toAbsolutePath().resolveSibling( localFile.getFileName() + ".md5" );
        Path sha1File = localFile.toAbsolutePath().resolveSibling( localFile.getFileName() + ".sha1" );

        assertTrue( "ChecksumPolicy.apply(FIX) md5 should exist.", Files.exists(md5File) && Files.isRegularFile(md5File) );
        assertTrue( "ChecksumPolicy.apply(FIX) sha1 should exist.", Files.exists(sha1File) && Files.isRegularFile(sha1File) );

        String actualMd5Contents = readChecksumFile( md5File );
        String actualSha1Contents = readChecksumFile( sha1File );

        String expectedMd5Contents = "360ccd01d8a0a2d94b86f9802c2fc548  artifact.jar";
        String expectedSha1Contents = "7dd8929150664f182db60ad15f20359d875f059f  artifact.jar";

        assertEquals( "ChecksumPolicy.apply(FIX) md5 contents:", expectedMd5Contents, actualMd5Contents );
        assertEquals( "ChecksumPolicy.apply(FIX) sha1 contents:", expectedSha1Contents, actualSha1Contents );
    }

    /**
     * Read the first line from the checksum file, and return it (trimmed).
     */
    private String readChecksumFile( Path checksumFile )
        throws Exception
    {
        FileReader freader = null;
        BufferedReader buf = null;

        try
        {
            freader = new FileReader( checksumFile.toFile() );
            buf = new BufferedReader( freader );
            return buf.readLine();
        }
        finally
        {
            if ( buf != null )
            {
                buf.close();
            }

            if ( freader != null )
            {
                freader.close();
            }
        }
    }

    private String createMessage( String settingType, String md5State, String sha1State )
    {
        StringBuilder msg = new StringBuilder();
        msg.append( "Expected result of ChecksumPolicy.apply(" );
        msg.append( settingType.toUpperCase() );
        msg.append( ") when working with " );
        if ( md5State == null )
        {
            msg.append( "NO" );
        }
        else
        {
            msg.append( "a " ).append( md5State.toUpperCase() );
        }

        msg.append( " MD5 and " );

        if ( sha1State == null )
        {
            msg.append( "NO" );
        }
        else
        {
            msg.append( "a " ).append( sha1State.toUpperCase() );
        }
        msg.append( " SHA1:" );

        return msg.toString();
    }

    private Properties createRequest()
    {
        Properties request = new Properties();

        request.setProperty( "url", "http://a.bad.hostname.maven.org/path/to/resource.txt" );

        return request;
    }

    private Path createTestableFiles( String md5State, String sha1State )
        throws Exception
    {
        Path sourceDir = getTestFile( "src/test/resources/checksums/" );
        Path destDir = getTestFile( "target/checksum-tests/" + name.getMethodName() + "/" );

        FileUtils.copyFileToDirectory( sourceDir.resolve("artifact.jar" ).toFile(), destDir.toFile() );

        if ( md5State != null )
        {
            Path md5File = sourceDir.resolve("artifact.jar.md5-" + md5State );
            assertTrue( "Testable file exists: " + md5File.getFileName() + ":", Files.exists(md5File) && Files.isRegularFile(md5File) );
            Path destFile = destDir.resolve("artifact.jar.md5" );
            FileUtils.copyFile( md5File.toFile(), destFile.toFile() );
        }

        if ( sha1State != null )
        {
            Path sha1File = sourceDir.resolve("artifact.jar.sha1-" + sha1State );
            assertTrue( "Testable file exists: " + sha1File.getFileName() + ":", Files.exists(sha1File) && Files.isRegularFile(sha1File) );
            Path destFile = destDir.resolve("artifact.jar.sha1" );
            FileUtils.copyFile( sha1File.toFile(), destFile.toFile() );
        }

        Path localFile = destDir.resolve("artifact.jar" );
        return localFile;
    }

    public static Path getTestFile( String path )
    {
        return Paths.get( org.apache.archiva.common.utils.FileUtils.getBasedir(), path );
    }

}
