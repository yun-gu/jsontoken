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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class JsonServerDescriptorDocument implements ServerDescriptorDocument {

  @SerializedName("verification_keys")
  private final Map<String, String> verificationKeys = Maps.newHashMap();

  public static JsonServerDescriptorDocument getDocument(String json) {
    return new Gson().fromJson(json, JsonServerDescriptorDocument.class);
  }

  @Override
  public PublicKey getVerificationKey(String keyId) {
    String magicKey = verificationKeys.get(keyId);

    if (magicKey == null) {
      return null;
    }

    String[] pieces = magicKey.split(Pattern.quote("."));
    if (pieces.length != 3) {
      throw new IllegalStateException("not a valid magic key: " + magicKey);
    }

    if (!pieces[0].equals("RSA")) {
      throw new IllegalStateException("unkown key type for magic key: " + pieces[0]);
    }

    String modulusString = pieces[1];
    String exponentString = pieces[2];

    byte[] modulusBytes = Base64.decodeBase64(modulusString);
    byte[] exponentBytes = Base64.decodeBase64(exponentString);

    BigInteger modulus = new BigInteger(modulusBytes);
    BigInteger exponent = new BigInteger(exponentBytes);

    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
    KeyFactory fac;
    try {
      fac = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("RSA key factory missing on platform", e);
    }
    try {
      return fac.generatePublic(spec);
    } catch (InvalidKeySpecException e) {
      throw new IllegalStateException("bad key in descripor doc: " + magicKey, e);
    }
  }
}
