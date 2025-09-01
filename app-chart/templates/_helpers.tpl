{{/*
Expand the name of the chart.
*/}}
{{- define "app-chart.name" -}}
{{- default .Chart.Name .Values.app.name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "app-chart.fullname" -}}
{{- if .Values.app.name }}
{{- .Values.app.name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "app-chart.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "app-chart.labels" -}}
helm.sh/chart: {{ include "app-chart.chart" . }}
{{ include "app-chart.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "app-chart.selectorLabels" -}}
app.kubernetes.io/name: {{ include "app-chart.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Generate TLS configuration based on domain
*/}}
{{- define "app-chart.tlsConfig" -}}
{{- $certMapping := .Values.ingress.tls.certMapping -}}
{{- $hosts := .Values.ingress.hosts -}}
{{- $tlsSecrets := dict -}}
{{- range $host := $hosts -}}
  {{- $domain := $host.host -}}
  {{- $secretName := "" -}}
  {{- range $certDomain, $certSecret := $certMapping -}}
    {{- if or (hasSuffix $certDomain $domain) (eq $certDomain $domain) -}}
      {{- $secretName = $certSecret -}}
    {{- end -}}
  {{- end -}}
  {{- if $secretName -}}
    {{- if not (hasKey $tlsSecrets $secretName) -}}
      {{- $_ := set $tlsSecrets $secretName (list $domain) -}}
    {{- else -}}
      {{- $currentHosts := index $tlsSecrets $secretName -}}
      {{- $_ := set $tlsSecrets $secretName (append $currentHosts $domain) -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- range $secret, $hostList := $tlsSecrets }}
  - hosts:
    {{- range $h := $hostList }}
    - {{ $h }}
    {{- end }}
    secretName: {{ $secret }}
{{- end -}}
{{- end }}