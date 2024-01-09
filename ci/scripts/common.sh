source /opt/concourse-java.sh

build() {
	run_maven clean native:compile -Pnative
}

setup_symlinks
cleanup_maven_repo "io.spring.concourse.googlechatnotifyresource"
