/*
 * Copyright (c) 2000, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.security.util;

/**
 * This class represents the <code>ResourceBundle</code>
 * for javax.security.auth and sun.security.
 *
 */
public class Resources_ja extends java.util.ListResourceBundle {

    private static final Object[][] contents = {

        // javax.security.auth.PrivateCredentialPermission
        {"invalid.null.input.s.", "null\u306E\u5165\u529B\u306F\u7121\u52B9\u3067\u3059"},
        {"actions.can.only.be.read.", "\u30A2\u30AF\u30B7\u30E7\u30F3\u306F'\u8AAD\u8FBC\u307F'\u306E\u307F\u53EF\u80FD\u3067\u3059"},
        {"permission.name.name.syntax.invalid.",
                "\u30A2\u30AF\u30BB\u30B9\u6A29\u540D[{0}]\u306E\u69CB\u6587\u304C\u7121\u52B9\u3067\u3059: "},
        {"Credential.Class.not.followed.by.a.Principal.Class.and.Name",
                "Credential\u30AF\u30E9\u30B9\u306E\u6B21\u306BPrincipal\u30AF\u30E9\u30B9\u304A\u3088\u3073\u540D\u524D\u304C\u3042\u308A\u307E\u305B\u3093"},
        {"Principal.Class.not.followed.by.a.Principal.Name",
                "Principal\u30AF\u30E9\u30B9\u306E\u6B21\u306B\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u540D\u304C\u3042\u308A\u307E\u305B\u3093"},
        {"Principal.Name.must.be.surrounded.by.quotes",
                "\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u540D\u306F\u5F15\u7528\u7B26\u3067\u56F2\u3080\u5FC5\u8981\u304C\u3042\u308A\u307E\u3059"},
        {"Principal.Name.missing.end.quote",
                "\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u540D\u306E\u6700\u5F8C\u306B\u5F15\u7528\u7B26\u304C\u3042\u308A\u307E\u305B\u3093"},
        {"PrivateCredentialPermission.Principal.Class.can.not.be.a.wildcard.value.if.Principal.Name.is.not.a.wildcard.value",
                "\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u540D\u304C\u30EF\u30A4\u30EB\u30C9\u30AB\u30FC\u30C9(*)\u5024\u3067\u306A\u3044\u5834\u5408\u3001PrivateCredentialPermission\u306EPrincipal\u30AF\u30E9\u30B9\u3092\u30EF\u30A4\u30EB\u30C9\u30AB\u30FC\u30C9(*)\u5024\u306B\u3059\u308B\u3053\u3068\u306F\u3067\u304D\u307E\u305B\u3093"},
        {"CredOwner.Principal.Class.class.Principal.Name.name",
                "CredOwner:\n\tPrincipal\u30AF\u30E9\u30B9={0}\n\t\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u540D={1}"},

        // javax.security.auth.x500
        {"provided.null.name", "null\u306E\u540D\u524D\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"provided.null.keyword.map", "null\u306E\u30AD\u30FC\u30EF\u30FC\u30C9\u30FB\u30DE\u30C3\u30D7\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"provided.null.OID.map", "null\u306EOID\u30DE\u30C3\u30D7\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},

        // javax.security.auth.Subject
        {"NEWLINE", "\n"},
        {"invalid.null.action.provided", "\u7121\u52B9\u306Anull\u30A2\u30AF\u30B7\u30E7\u30F3\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"invalid.null.Class.provided", "\u7121\u52B9\u306Anull\u30AF\u30E9\u30B9\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"Subject.", "\u30B5\u30D6\u30B8\u30A7\u30AF\u30C8:\n"},
        {".Principal.", "\t\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB: "},
        {".Public.Credential.", "\t\u516C\u958B\u8CC7\u683C: "},
        {".Private.Credential.", "\t\u975E\u516C\u958B\u8CC7\u683C: "},
        {".Private.Credential.inaccessible.",
                "\t\u975E\u516C\u958B\u8CC7\u683C\u306B\u306F\u30A2\u30AF\u30BB\u30B9\u3067\u304D\u307E\u305B\u3093\n"},
        {"Subject.is.read.only", "\u30B5\u30D6\u30B8\u30A7\u30AF\u30C8\u306F\u8AAD\u53D6\u308A\u5C02\u7528\u3067\u3059"},
        {"attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set",
                "java.security.Principal\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u3067\u306F\u306A\u3044\u30AA\u30D6\u30B8\u30A7\u30AF\u30C8\u3092\u3001\u30B5\u30D6\u30B8\u30A7\u30AF\u30C8\u306E\u30D7\u30EA\u30F3\u30B7\u30D1\u30EB\u30FB\u30BB\u30C3\u30C8\u306B\u8FFD\u52A0\u3057\u3088\u3046\u3068\u3057\u307E\u3057\u305F"},
        {"attempting.to.add.an.object.which.is.not.an.instance.of.class",
                "{0}\u306E\u30A4\u30F3\u30B9\u30BF\u30F3\u30B9\u3067\u306F\u306A\u3044\u30AA\u30D6\u30B8\u30A7\u30AF\u30C8\u3092\u8FFD\u52A0\u3057\u3088\u3046\u3068\u3057\u307E\u3057\u305F"},

        // javax.security.auth.login.AppConfigurationEntry
        {"LoginModuleControlFlag.", "LoginModuleControlFlag: "},

        // javax.security.auth.login.LoginContext
        {"Invalid.null.input.name", "\u7121\u52B9\u306Anull\u5165\u529B: \u540D\u524D"},
        {"No.LoginModules.configured.for.name",
         "{0}\u7528\u306B\u69CB\u6210\u3055\u308C\u305FLoginModules\u306F\u3042\u308A\u307E\u305B\u3093"},
        {"invalid.null.Subject.provided", "\u7121\u52B9\u306Anull\u30B5\u30D6\u30B8\u30A7\u30AF\u30C8\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"invalid.null.CallbackHandler.provided",
                "\u7121\u52B9\u306Anull CallbackHandler\u304C\u6307\u5B9A\u3055\u308C\u307E\u3057\u305F"},
        {"null.subject.logout.called.before.login",
                "null\u30B5\u30D6\u30B8\u30A7\u30AF\u30C8 - \u30ED\u30B0\u30A4\u30F3\u3059\u308B\u524D\u306B\u30ED\u30B0\u30A2\u30A6\u30C8\u304C\u547C\u3073\u51FA\u3055\u308C\u307E\u3057\u305F"},
        {"Login.Failure.all.modules.ignored",
                "\u30ED\u30B0\u30A4\u30F3\u5931\u6557: \u3059\u3079\u3066\u306E\u30E2\u30B8\u30E5\u30FC\u30EB\u306F\u7121\u8996\u3055\u308C\u307E\u3059"},

        // sun.security.provider.PolicyParser
        {"duplicate.keystore.domain.name","\u91CD\u8907\u3059\u308B\u30AD\u30FC\u30B9\u30C8\u30A2\u30FB\u30C9\u30E1\u30A4\u30F3\u540D: {0}"},
        {"duplicate.keystore.name","\u91CD\u8907\u3059\u308B\u30AD\u30FC\u30B9\u30C8\u30A2\u540D: {0}"},
        {"number.", "\u6570 "},
        {"expected.expect.read.end.of.file.",
                "[{0}]\u3067\u306F\u306A\u304F[\u30D5\u30A1\u30A4\u30EB\u306E\u7D42\u308F\u308A]\u304C\u8AAD\u307F\u8FBC\u307E\u308C\u307E\u3057\u305F"},
        {"expected.read.end.of.file.",
                "[;]\u3067\u306F\u306A\u304F[\u30D5\u30A1\u30A4\u30EB\u306E\u7D42\u308F\u308A]\u304C\u8AAD\u307F\u8FBC\u307E\u308C\u307E\u3057\u305F"},
        {"line.number.msg", "\u884C{0}: {1}"},
        {"line.number.expected.expect.found.actual.",
                "\u884C{0}: [{1}]\u3067\u306F\u306A\u304F[{2}]\u304C\u691C\u51FA\u3055\u308C\u307E\u3057\u305F"},

        // sun.security.pkcs11.SunPKCS11
        {"PKCS11.Token.providerName.Password.",
                "PKCS11\u30C8\u30FC\u30AF\u30F3[{0}]\u30D1\u30B9\u30EF\u30FC\u30C9: "},
    };


    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     *
     * @return the contents of this <code>ResourceBundle</code>.
     */
    @Override
    public Object[][] getContents() {
        return contents;
    }
}

