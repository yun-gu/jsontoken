/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.oauth.jsontoken;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class RsaSHA256Verifier implements Verifier {

  private final PublicKey verificationKey;
  private final Signature signer;

  public RsaSHA256Verifier(PublicKey verificationKey) {
    this.verificationKey = verificationKey;
    try {
      this.signer = Signature.getInstance("SHA256withRSA");
      this.signer.initVerify(verificationKey);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("platform is missing RSAwithSHA256 signature alg", e);
    } catch (InvalidKeyException e) {
      throw new IllegalStateException("key is invalid", e);
    };
  }

  @Override
  public void verifySignature(byte[] source, byte[] signature) throws SignatureException {
    try {
      signer.initVerify(verificationKey);
    } catch (InvalidKeyException e) {
      throw new RuntimeException("key someone become invalid since calling the constructor");
    }
    signer.update(source);
    if (!signer.verify(signature)) {
      throw new SignatureException("signature did not verify");
    }
  }
}
