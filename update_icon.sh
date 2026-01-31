#!/bin/bash
# Script pour mettre à jour l'icône de l'app Muscunombre
# 
# Usage: ./update_icon.sh chemin/vers/ton/image.png
#
# L'image doit être carrée (idéalement 512x512 ou plus)

if [ -z "$1" ]; then
    echo "❌ Erreur: Spécifie le chemin vers ton image"
    echo "Usage: ./update_icon.sh chemin/vers/ton/image.png"
    exit 1
fi

if [ ! -f "$1" ]; then
    echo "❌ Erreur: Le fichier '$1' n'existe pas"
    exit 1
fi

SOURCE_IMAGE="$1"
RES_DIR="app/src/main/res"

echo "🎨 Génération des icônes depuis: $SOURCE_IMAGE"

# Générer les différentes tailles
magick "$SOURCE_IMAGE" -resize 48x48 "$RES_DIR/mipmap-mdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 48x48 "$RES_DIR/mipmap-mdpi/ic_launcher_round.png"
echo "  ✅ mdpi (48x48)"

magick "$SOURCE_IMAGE" -resize 72x72 "$RES_DIR/mipmap-hdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 72x72 "$RES_DIR/mipmap-hdpi/ic_launcher_round.png"
echo "  ✅ hdpi (72x72)"

magick "$SOURCE_IMAGE" -resize 96x96 "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 96x96 "$RES_DIR/mipmap-xhdpi/ic_launcher_round.png"
echo "  ✅ xhdpi (96x96)"

magick "$SOURCE_IMAGE" -resize 144x144 "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 144x144 "$RES_DIR/mipmap-xxhdpi/ic_launcher_round.png"
echo "  ✅ xxhdpi (144x144)"

magick "$SOURCE_IMAGE" -resize 192x192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"
magick "$SOURCE_IMAGE" -resize 192x192 "$RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png"
echo "  ✅ xxxhdpi (192x192)"

echo ""
echo "🎉 Icônes générées avec succès !"
echo ""
echo "Pour reconstruire l'APK, lance:"
echo "  JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug"
