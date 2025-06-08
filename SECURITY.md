# Security Policy

## Supported Versions

We actively support the following versions of BitcoinWidget2:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability in BitcoinWidget2, please report it responsibly:

### Please DO NOT:
- Open a public GitHub issue for security vulnerabilities
- Discuss the vulnerability publicly until it has been addressed

### Please DO:
1. **Email us directly** at [your-security-email@domain.com] with:
   - Description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact assessment
   - Suggested fix (if any)

2. **Wait for acknowledgment** - We'll respond within 48 hours

3. **Allow time for fix** - We'll work to address the issue promptly

## Security Features

BitcoinWidget2 implements several security measures:

### API Security
- **HTTPS Only**: All API communications use secure HTTPS connections
- **Request Validation**: Input validation and sanitization for all API requests
- **Rate Limiting**: Built-in protection against API abuse
- **Error Handling**: Secure error messages that don't expose sensitive information

### Data Privacy
- **No Personal Data**: The app doesn't collect or store personal information
- **Local Storage**: All data is stored locally on the device
- **No Analytics**: No tracking or analytics data collection
- **Minimal Permissions**: Only requests necessary Android permissions

### Network Security
- **Certificate Pinning**: Protection against man-in-the-middle attacks
- **Secure Connections**: All network traffic is encrypted
- **API Key Protection**: Secure handling of API credentials

## Security Best Practices for Users

### Installation
- Only install from official sources (GitHub releases or Google Play Store)
- Verify APK signatures before installation
- Keep the app updated to the latest version

### Usage
- Use on trusted Wi-Fi networks when possible
- Be cautious of price alerts and notifications
- Review app permissions regularly

## Vulnerability Response Process

1. **Reception**: Security report received and acknowledged (< 48 hours)
2. **Assessment**: Vulnerability assessed and validated (1-3 days)
3. **Development**: Fix developed and tested (varies by complexity)
4. **Release**: Security update released (ASAP for critical issues)
5. **Disclosure**: Public disclosure after fix is deployed (7-30 days)

## Security Updates

Security updates are our highest priority:

- **Critical vulnerabilities**: Emergency release within 24-48 hours
- **High severity**: Release within 1 week
- **Medium/Low severity**: Included in next regular release

## Hall of Fame

We appreciate security researchers who help improve our security:

*No reports yet - be the first!*

## Contact

For security-related questions or reports:
- **Security Email**: [your-security-email@domain.com]
- **GPG Key**: [Link to public GPG key if available]

Thank you for helping keep BitcoinWidget2 secure!