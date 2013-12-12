/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *               Bademus
 */

package org.github.bademux.feedly.api;

import org.github.bademux.feedly.api.oauth2.FeedlyCredential;
import org.github.bademux.feedly.api.service.DevFeedly;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Test login process */
public class CredentialIntegrationTest extends AbstractIntegrationTest {

  @Before
  @After
  public void setUp() throws IOException {
    if (DATA_STORE_DIR.exists()) {
      assertTrue(deleteDir(DATA_STORE_DIR));
    }

    assertFalse(DATA_STORE_DIR.exists());
  }

  @Test
  public void testCredential() throws IOException {
    //Login
    FeedlyCredential credential = login();

    //check
    assertTrue(DATA_STORE_DIR.exists());
    assertNotNull(credential);
    assertNotNull(credential.getUserId());
    assertNotNull(credential.getPlan());
    assertNotNull(credential.getClock());
    assertNotNull(credential.getExpirationTimeMilliseconds());
    assertNotNull(credential.getRefreshToken());

    String accessToken = credential.getAccessToken();
    assertNotNull(accessToken);

    //refresh access token and check
    assertTrue(credential.refreshToken());
    assertNotEquals(accessToken, credential.getAccessToken());

    //setup Feedly service
    service = new DevFeedly.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
    //log out
    service.clearCredential();
    assertNull(credential.getRefreshToken());
  }

  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      for (String children : dir.list()) {
        if (!deleteDir(new File(dir, children))) {
          return false;
        }
      }
    }
    return dir.delete(); // The directory is empty now and can be deleted.
  }
}