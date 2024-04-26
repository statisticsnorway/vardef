# OpenAPI

Work in progress OpenAPI spec for Vardef.

## Render interactive documentation

There are many tools to choose from for rendering OpenAPI specs. The following are just suggestions.

### Online

1. Visit <https://editor-next.swagger.io/>.
1. Paste the contents of <./vardef-openapi-spec.yaml> into the editor.

### Local

1. Install Scalar `npm -g install @scalar/cli`.
1. Run `scalar reference vardef-openapi-spec.yaml --watch` from this directory.