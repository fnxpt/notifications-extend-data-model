# Docker inheritance
FROM repo.backbase.com/backbase-docker-releases/notifications-service:DBS-2.19.0

COPY target/classes/apiExtension.yml /app/WEB-INF/classes