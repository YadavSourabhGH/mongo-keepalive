# Package Publishing Guide

## ✅ Successfully Published

- **Rust** → crates.io ✓
- **Ruby** → RubyGems.org ✓
- **C#** → NuGet.org ✓
- **Dart** → pub.dev ✓

---

## � Ready to Publish (Complete These Steps)

### 1. **Go Module** (Tag-Based, No Registry)

**Status:** Local tags created, push may be in progress

Go modules use GitHub tags for versioning. Verify the tag is pushed:

```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive
git ls-remote --tags origin | grep go/v1.0.0
```

If not listed, push manually:
```bash
git push origin go/v1.0.0
```

**Users can then install with:**
```bash
go get github.com/YadavSourabhGH/mongo-keepalive/go@v1.0.0
```

✅ **No further action needed** - Go packages are distributed via GitHub tags.

---

### 2. **PHP** (Packagist)

**Status:** Maven Central config added ✓

**Step 1: Push tag to GitHub**
```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive
git push origin php/v1.0.0
```

**Step 2: Register on Packagist**
1. Visit https://packagist.org
2. Sign in with your GitHub account
3. Click the **"Submit"** button (top right)
4. Enter repository URL: `https://github.com/YadavSourabhGH/mongo-keepalive`
5. Click "Check Repository"

Packagist will auto-detect `composer.json` in the `php/` directory.

**Step 3: Enable auto-updates (recommended)**
1. Go to your package page on Packagist
2. Click "Edit Package"
3. Enable "GitHub Service Hook" or set up a webhook

**Users can then install with:**
```bash
composer require yadavsourabhgh/mongo-keepalive
```

---

### 3. **Node.js** (npm)

```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive/node

# Login to npm (if not already)
npm login

# Publish
npm publish --access public
```

**If name conflict:** Update `node/package.json`:
```json
{
  "name": "@yadavsourabhgh/mongo-keepalive"
}
```
Then retry: `npm publish --access public`

---

### 4. **Python** (PyPI)

```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive/python

# Install publishing tools
python -m pip install build twine

# Build distribution
python -m build

# Upload to PyPI
twine upload dist/*
```

You'll be prompted for PyPI credentials. Use an API token for security.

**If name conflict:** Update `python/pyproject.toml`:
```toml
name = "mongo-keepalive-yadavsourabhgh"
```

---

### 5. **Java** (Maven Central via Sonatype)

**Status:** Maven Central config added ✓

**Prerequisites:**

1. **Create Sonatype account:**
   - Visit https://central.sonatype.com
   - Sign up and create a namespace (e.g., `com.mongokeepalive`)

2. **Generate GPG key:**
   ```bash
   # Generate key
   gpg --gen-key
   
   # List keys to get KEY_ID
   gpg --list-secret-keys --keyid-format=long
   
   # Upload public key to keyserver
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Configure Maven settings** (`~/.m2/settings.xml`):
   ```xml
   <settings>
     <servers>
       <server>
         <id>ossrh</id>
         <username>YOUR_SONATYPE_USERNAME</username>
         <password>YOUR_SONATYPE_PASSWORD</password>
       </server>
     </servers>
   </settings>
   ```

**Publish:**
```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive/java
mvn clean deploy
```

The artifact will be staged at Sonatype. Log in to https://s01.oss.sonatype.org to release it.

---

### 6. **Kotlin** (Maven Central via Sonatype)

**Status:** Maven Central config added ✓

Follow the same prerequisites as Java above, then:

```bash
cd /Users/sourabhyadav/Downloads/mongo-keepalive/kotlin
mvn clean deploy
```

---

## 📦 Package URLs (Once Published)

| Language | Registry URL |
|----------|-------------|
| Rust | https://crates.io/crates/mongo-keepalive |
| Ruby | https://rubygems.org/gems/mongo-keepalive |
| C# | https://www.nuget.org/packages/MongoKeepAlive |
| Dart | https://pub.dev/packages/mongo_keepalive |
| Go | https://pkg.go.dev/github.com/YadavSourabhGH/mongo-keepalive/go |
| PHP | https://packagist.org/packages/yadavsourabhgh/mongo-keepalive |
| Node.js | https://www.npmjs.com/package/mongo-keepalive |
| Python | https://pypi.org/project/mongo-keepalive |
| Java | https://central.sonatype.com/artifact/com.mongokeepalive/mongo-keepalive |
| Kotlin | https://central.sonatype.com/artifact/com.mongokeepalive/mongo-keepalive-kotlin |

---

## 🔧 Troubleshooting

### Git Push Timeouts

If tag pushes keep timing out:

```bash
# Increase buffer and disable timeout
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999

# Push tags individually
git push origin go/v1.0.0
git push origin php/v1.0.0
```

Or use SSH instead of HTTPS:
```bash
git remote set-url origin git@github.com:YadavSourabhGH/mongo-keepalive.git
git push origin --tags
```

### Package Name Conflicts

- **npm:** Use scoped name `@yadavsourabhgh/mongo-keepalive`
- **PyPI:** Append modifier `mongo-keepalive-yadavsourabhgh`
- **Others:** First-come-first-served basis

### Maven GPG Signing Issues

If `mvn deploy` fails with GPG errors:

```bash
# Test GPG
echo "test" | gpg --clearsign

# Set GPG TTY
export GPG_TTY=$(tty)

# Use pinentry-mac on macOS
brew install pinentry-mac
echo "pinentry-program $(which pinentry-mac)" >> ~/.gnupg/gpg-agent.conf
gpgconf --kill gpg-agent
```

---

## ✨ Quick Checklist

- [x] Rust published
- [x] Ruby published  
- [x] C# published
- [x] Dart published
- [x] Maven Central config added for Java/Kotlin
- [ ] Push Go tag to GitHub
- [ ] Push PHP tag to GitHub
- [ ] Register PHP on Packagist
- [ ] Publish Node.js to npm
- [ ] Publish Python to PyPI
- [ ] Set up GPG and publish Java to Maven Central
- [ ] Publish Kotlin to Maven Central

---

**All code is production-ready and tested ✓**  
**Repository:** https://github.com/YadavSourabhGH/mongo-keepalive
