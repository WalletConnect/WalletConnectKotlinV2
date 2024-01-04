#!/bin/bash


# Check if exactly one argument is provided
if [ $# -ne 1 ]; then
    echo "Error: Exactly one argument is required."
    echo "Usage: $0 path_to_output_file"
    exit 1
fi


#  Fetch latest tag
git tag -l | xargs git tag -d >/dev/null
git fetch --tags --quiet
tag=$(git ls-remote --quiet --tags --sort=committerdate | grep -o 'BOM.*' | tail -1)

echo "Latest release: $tag"

check_changes() {
  local module_path=$1
  local output_variable_name=$2

  changes=$(git diff --name-only "$tag" -- "$module_path/")

  if [ -n "$changes" ]; then
    export "${output_variable_name}=true"
  else
    export "${output_variable_name}=false"
  fi
}

check_changes "foundation" "FOUNDATION_MODULE_CHANGED"
check_changes "core/android" "CORE_MODULE_CHANGED"
check_changes "core/modal" "MODAL_CORE_MODULE_CHANGED"
check_changes "protocol/sign" "SIGN_MODULE_CHANGED"
check_changes "protocol/auth" "AUTH_MODULE_CHANGED"
check_changes "protocol/chat" "CHAT_MODULE_CHANGED"
check_changes "protocol/notify" "NOTIFY_MODULE_CHANGED"
check_changes "product/web3wallet" "WEB_3_WALLET_MODULE_CHANGED"
check_changes "product/walletconnectmodal" "WC_MODAL_MODULE_CHANGED"
check_changes "product/web3modal" "WEB_3_MODAL_MODULE_CHANGED"

echo "Module changes output saved to $1"

echo "FOUNDATION_MODULE_CHANGED=$FOUNDATION_MODULE_CHANGED" >> "$1"
echo "CORE_MODULE_CHANGED=$CORE_MODULE_CHANGED" >> "$1"
echo "MODAL_CORE_MODULE_CHANGED=$MODAL_CORE_MODULE_CHANGED" >> "$1"
echo "SIGN_MODULE_CHANGED=$SIGN_MODULE_CHANGED" >> "$1"
echo "AUTH_MODULE_CHANGED=$AUTH_MODULE_CHANGED" >> "$1"
echo "CHAT_MODULE_CHANGED=$CHAT_MODULE_CHANGED" >> "$1"
echo "NOTIFY_MODULE_CHANGED=$NOTIFY_MODULE_CHANGED" >> "$1"
echo "WEB_3_WALLET_MODULE_CHANGED=$WEB_3_WALLET_MODULE_CHANGED" >> "$1"
echo "WC_MODAL_MODULE_CHANGED=$WC_MODAL_MODULE_CHANGED" >> "$1"
echo "WEB_3_MODAL_MODULE_CHANGED=$WEB_3_MODAL_MODULE_CHANGED" >> "$1"