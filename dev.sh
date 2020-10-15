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
  echo "${green} update-docs${reset}        Update docs"
}

docker_compose() {
  command='command'
  if [[ "$USER" != "root" ]] && ! groups "$USER" | grep -q '\bdocker\b'; then
    command='sudo'
  fi
  ${command} docker-compose -p 'P4_parking_system' -f '.dev/docker-compose.yml' "$@"
}

update_docs() {
  curl 'http://localhost:8081/api-docs' \
    | jq '.info.description = "<a href=\"https://github.com/np111/P5_safetynet\">View Source on GitHub</a>" |
          .servers = []' >'./docs/api-docs.json'
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
update-docs)
  update_docs "$@"
  ;;
*)
  echo "${red2}Error: '${command}' is not a dev command.${reset2}" >&2
  exit 1
  ;;
esac

exit "$?"
