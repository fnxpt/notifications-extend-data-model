# Docker inheritance
FROM repo.backbase.com/backbase-docker-releases/notifications-service:DBS-2.18.1

COPY target/classes/apiExtension.yml /app/WEB-INF/classes