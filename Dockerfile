FROM ubuntu:jammy-20231004

ARG root=.
ARG executable=build/native/nativeCompile/google-chat-notify

COPY ${root}/assets/ /opt/resource/
COPY ${executable} /artifact/google-chat-notify

RUN export DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install --no-install-recommends -y tzdata ca-certificates curl
RUN ln -fs /usr/share/zoneinfo/UTC /etc/localtime
RUN dpkg-reconfigure --frontend noninteractive tzdata
RUN rm -rf /var/lib/apt/lists/*

RUN chmod +x /opt/resource/check /opt/resource/in /opt/resource/out
