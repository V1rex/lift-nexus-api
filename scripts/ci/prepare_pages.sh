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

# Static assets for landing page
if [ -d docs/assets ]; then
  cp -r docs/assets "$PAGES_DIR/assets"
else
  echo "Assets directory missing: docs/assets"
fi

# MkDocs generated documentation
if [ -d build/site ]; then
  cp -r build/site/* "$PAGES_DIR/site/"
else
  echo "MkDocs directory missing: build/site"
  exit 1
fi

# JaCoCo coverage report
if [ -d target/site/jacoco ]; then
  cp -r target/site/jacoco/* "$PAGES_DIR/coverage/"
else
  echo "JaCoCo directory missing: target/site/jacoco"
  exit 1
fi

# Javadoc
if [ -d target/reports/apidocs ]; then
  cp -r target/reports/apidocs/* "$PAGES_DIR/javadoc/"
else
  echo "Javadoc directory missing: target/reports/apidocs"
  exit 1
fi

# Surefire test reports
if [ -d target/surefire-reports ]; then
  cp -r target/surefire-reports/* "$PAGES_DIR/tests/"
else
  echo "Surefire directory missing: target/surefire-reports"
fi

echo "GitHub Pages output prepared in $PAGES_DIR/"