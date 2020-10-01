[![](https://jitpack.io/v/yueban/flipper-sqlcipher-plugin.svg)](https://jitpack.io/#yueban/flipper-sqlcipher-plugin)

## preview

![preview](preview.png)

## How to use

1. Add the JitPack repository to your build file

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.yueban:flipper-sqlcipher-plugin:1.0.2'
}
```

3. Create a `SqlCipherDatabaseDriver` to init your `DatabasesFlipperPlugin`

```kotlin
val client = AndroidFlipperClient.getInstance(this)

// add database plugin
client.addPlugin(
    DatabasesFlipperPlugin(
        SqlCipherDatabaseDriver(this, object : DatabasePasswordProvider {
            override fun getDatabasePassword(databaseFile: File): String {
                return if ("your database file name" == databaseFile.name) {
                    return "your database password"
                } else {
                    ""
                }
            }
        })
    )
)

client.start()
```

check [sample](sample) for more details.

## minSdkVersioin

The minSdkVersion is 16, cause this library depends on `android-database-sqlcipher` library which minSdkVersion is 16.