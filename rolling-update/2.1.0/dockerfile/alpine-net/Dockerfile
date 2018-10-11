FROM alpine:latest
RUN apk add --no-cache bash
RUN apk add --no-cache curl
RUN apk add --no-cache nmap
RUN mkdir -p /root/shell_script
ADD curl.sh /root/shell_script
ADD ncat.sh /root/shell_script
RUN chmod 775 /root/shell_script/curl.sh
RUN chmod 775 /root/shell_script/ncat.sh