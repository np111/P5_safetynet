#!/bin/bash
set -Eeuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

. ./.dev/colors.inc.sh

print_usage() {
  echo "${yellow}Usage:${reset}"
  echo " $0 <command> [options]"
  echo
  echo "${yellow}Available commands:${reset}"
  echo "${green} docker [...]${reset}       Manually use docker-compose commands"
  echo "${green} docker up -d${reset}       Create and start development containers (in background)"
  echo "${green} docker down${reset}        Stop and remove development containers"
  echo "${green} docker logs${reset}        Print development containers logs"
  echo "${green} generate-docs${reset}      Generate docs"
  echo "${green} publish-docs${reset}       Publish docs"
}

docker_compose() {
  command='command'
  if [[ "$USER" != "root" ]] && ! groups "$USER" | grep -q '\bdocker\b'; then
    command='sudo'
  fi
  ${command} docker-compose -p 'P4_parking_system' -f '.dev/docker-compose.yml' "$@"
}

generate_docs() {
  # Copy site base
  docs_dir='./docs'
  rm -rf "${docs_dir}"
  cp -R '.site/' "${docs_dir}"

  # Copy openapi specification
  curl 'http://localhost:8081/api-docs' |
    jq '.info.description = "<a href=\"https://github.com/np111/P5_safetynet\">View Source on GitHub</a>" |
          .servers = []' >"${docs_dir}/httpapi/openapi.json"

  # Generate javadoc
  javadoc_dir="${docs_dir}/javadoc"
  mvn -Pdelombok-javadoc clean compile javadoc:javadoc
  mkdir -p "${javadoc_dir}"
  rm -rf "${javadoc_dir}/api" && cp -R './api/target/site/apidocs' "${javadoc_dir}/api"
  rm -rf "${javadoc_dir}/server" && cp -R './server/target/site/apidocs' "${javadoc_dir}/server"
}

publish_docs() {
  origin="$(git config --get remote.origin.url)"
  cd docs
  rm -rf .git
  git init
  git remote add origin "${origin}"
  git checkout --orphan docs
  git add .
  git commit -m 'Publish docs'
  git push -f origin docs
}

# main
if [ $# -lt 1 ]; then
  print_usage
  exit 0
fi

command="$1"
shift
case "$command" in
docker)
  docker_compose "$@"
  ;;
generate-docs)
  generate_docs "$@"
  ;;
publish-docs)
  publish_docs "$@"
  ;;
*)
  echo "${red2}Error: '${command}' is not a dev command.${reset2}" >&2
  exit 1
  ;;
esac

exit "$?"
