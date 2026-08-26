{{- define "wafer-inspection.name" -}}
wafer-inspection
{{- end }}

{{- define "wafer-inspection.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "wafer-inspection.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{- define "wafer-inspection.labels" -}}
app.kubernetes.io/name: {{ include "wafer-inspection.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" }}
{{- end }}

