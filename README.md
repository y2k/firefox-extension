## Пример манифеста

```json
{
    "manifest_version": 2,
    "name": "sample-extension",
    "version": "1.0.2",
    "browser_specific_settings": {
        "gecko": {
            "id": "extension1@y2k.work",
            "update_url": "https://y2k.github.io/extension/updates.json"
        }
    },
    "content_scripts": [
        {
            "js": [
                "extension.js"
            ],
            "matches": [
                "https://wikipedia.org/*"
            ],
            "run_at": "document_end"
        }
    ]
}
```

## Обновления

https://extensionworkshop.com/documentation/manage/updating-your-extension/

```json
{
  "addons": {
    "{abcd1234-1abc-1234-12ab-abcdef123456}": {
      "updates": [
        {
          "version": "0.1",
          "update_link": "https://example.com/addon-0.1.xpi"
        },
        {
          "version": "0.2",
          "update_link": "http://example.com/addon-0.2.xpi",
          "update_hash": "sha256:fe93c2156f05f20621df1723b0f39c8ab28cdbeec342efa95535d3abff932096"
        },
        {
          "version": "0.3",
          "update_link": "https://example.com/addon-0.3.xpi",
          "applications": {
            "gecko": { "strict_min_version": "44" }
          }
        }
      ]
    }
  }
}
```
