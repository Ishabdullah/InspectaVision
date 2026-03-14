# SSH Deployment Setup Guide

This guide explains how to configure SSH deployment for the InspectaVision Android app.

## Overview

The GitHub Actions workflow can automatically deploy built APKs to a remote server via SSH after successful builds.

## Required GitHub Secrets

Navigate to your repository settings → Secrets and variables → Actions, then add the following secrets:

### SSH Connection Secrets

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `SSH_HOST` | The hostname or IP of your deployment server | `192.168.1.100` or `deploy.example.com` |
| `SSH_USER` | The SSH username for deployment | `deploy` or `ubuntu` |
| `SSH_PRIVATE_KEY` | The private SSH key for authentication | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### Optional: Release Signing Secrets

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `RELEASE_KEYSTORE` | Base64-encoded keystore file | `MIIJ...` (base64 of .jks file) |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password | `myKeystorePassword` |
| `RELEASE_KEY_ALIAS` | Key alias within keystore | `upload` |
| `RELEASE_KEY_PASSWORD` | Key password | `myKeyPassword` |

## Setting Up SSH Key Pair

### On Your Deployment Server

```bash
# Generate a new SSH key pair (on your local machine)
ssh-keygen -t ed25519 -C "inspectavision-deploy" -f inspectavision_deploy

# Copy the public key to your deployment server
ssh-copy-id -i inspectavision_deploy.pub user@your-server.com

# Or manually add to authorized_keys
cat inspectavision_deploy.pub | ssh user@your-server.com "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

### Add Private Key to GitHub Secrets

```bash
# Copy the private key content
cat inspectavision_deploy | pbcopy  # macOS
# or
cat inspectavision_deploy | xclip -selection clipboard  # Linux

# Paste into GitHub Secrets as SSH_PRIVATE_KEY
```

## Server Directory Structure

The workflow creates deployments in the following structure:

```
/home/<SSH_USER>/deploys/inspectavision/android/
├── 20260314_204500/     # Timestamped build
│   └── app-release.apk
├── 20260314_210000/     # Another build
│   └── app-release.apk
└── latest -> 20260314_210000/  # Symlink to latest
```

## Manual Testing

Test SSH connectivity before running the workflow:

```bash
# Test SSH connection
ssh -i inspectavision_deploy user@your-server.com "echo 'Connection successful!'"

# Test file transfer
scp -i inspectavision_deploy test.apk user@your-server.com:/tmp/
```

## Workflow Triggers

The SSH deployment runs when:
1. Manual workflow dispatch with `deploy_via_ssh: true`
2. Push to `main` branch (automatic)

## Security Considerations

1. **Use dedicated deploy user**: Create a separate user account for deployments
2. **Restrict SSH key**: Consider adding command restrictions in `authorized_keys`
3. **Use firewall rules**: Limit SSH access to GitHub Actions IP ranges
4. **Rotate keys regularly**: Update SSH keys periodically
5. **Audit logs**: Monitor `/var/log/auth.log` for deployment activity

## Troubleshooting

### Connection Refused
- Verify SSH is running: `sudo systemctl status sshd`
- Check firewall: `sudo ufw status`
- Verify host key in known_hosts

### Permission Denied
- Ensure public key is in `~/.ssh/authorized_keys`
- Check file permissions: `chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys`

### Workflow Fails at SSH Step
- Check secret names are exact (case-sensitive)
- Verify private key format (should start with `-----BEGIN`)
- Enable debug logging in workflow by adding `ACTIONS_STEP_DEBUG: true` secret

## Alternative: Deploy to GitHub Releases

Instead of SSH deployment, you can tag a commit to create a GitHub Release:

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This triggers the `create_release` job which uploads the APK as a release asset.
