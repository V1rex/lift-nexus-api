#!/usr/bin/env bash
set -euo pipefail

PAGES_DIR="gh-pages"

echo "Preparing GitHub Pages output..."

rm -rf "$PAGES_DIR"
mkdir -p "$PAGES_DIR/coverage" "$PAGES_DIR/javadoc" "$PAGES_DIR/tests" "$PAGES_DIR/site"

# Landing page + API docs
cp docs/index.html "$PAGES_DIR/"
cp docs/api.html "$PAGES_DIR/"
cp target/openapi.json "$PAGES_DIR/"

# Custom domain
echo "lift-nexus.amine-bahij.dev" > "$PAGES_DIR/CNAME"

# Static assets for landing page
if [ -d docs/assets ]; then
  cp -r docs/assets "$PAGES_DIR/assets"
fi

# MkDocs generated documentation
if [ -d build/site ]; then
  cp -r build/site/* "$PAGES_DIR/site/"
fi

# JaCoCo coverage report
if [ -d target/site/jacoco ]; then
  cp -r target/site/jacoco/* "$PAGES_DIR/coverage/"
fi

# Javadoc
if [ -d target/site/apidocs ]; then
  cp -r target/site/apidocs/* "$PAGES_DIR/javadoc/"
fi

# Surefire test reports
if [ -d target/surefire-reports ]; then
  cp -r target/surefire-reports/* "$PAGES_DIR/tests/"
fi

echo "GitHub Pages output prepared in $PAGES_DIR/"