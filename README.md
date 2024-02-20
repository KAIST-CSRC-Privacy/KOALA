# KOALA - KAIST Open-source Anonymization Platform
 
 **KOALA**(**K**aist **O**pen-source **A**nonymization p**LA**tform) is a data anonymization platform designed to help organizations protect sensitive information while still allowing them to leverage valuable data. By utilizing the ARX API, KOALA can quickly and easily anonymize structured data.

## Introduction
### Structured data

- Anonymization

  The anonymization in KOALA performes based on privacy models such as k-anonymity, l-diversity, t-closeness, and differential privacy.

- Feature Analysis

  KOALA provides a feature analysis, which gives the statistical information on the anonymized data.

  This information can be used as a decent feedback for choosing other privacy models or adjusting parameters.

### Unstructured data
- Image anonymization

  KOALA provides image blurring, masking, pixelation, and scrambling features for anonymization.

  If there is a human face in the image (photo), please check the `Face Recognition` button!

  The anonymization features will operate on human faces only.

- Image Encryption

  KOALA supports its own image encryption feature through passwords.

  By clicking the Encrypt Image button and setting a password, it will be encrypted into `Koala Encrypted File (.kenc)`.

  kenc can be decrypted within the KOALA program, and other encrypted images cannot be decrypted.


## Prerequisites
Before you begin, ensure you have met the following requirements:
- Java 19 or higher
- Apache Maven
You can check your Java version by running `java -version` and your Maven version by running `mvn -v` in your terminal.

## Dependencies

**KOALA** uses the following libraries, which are automatically managed by Maven:

- **OpenCV**
- **ARX Data Anonymization Tool**


## Build

Clone the project using Git or download it:

```bash
git clone https://github.com/KAIST-CSRC-Privacy/KOALA.git
cd KOALA
```
Use Maven to install dependencies and build the project:
```bash
mvn install
```

Download the Koala DB file and ensure it is located at `(executable path)/data/koala.db` to use it properly.

## Usage
To run the program:
```bash
java -jar KOALA-0.1.jar
```

## Authors
### Project Leader
- [Seung Hwan Ryu](https://github.com/deepryu) (deepryu@kaist.ac.kr)

### Developer
- [Yongki Hong](https://github.com/Bravery724) (ykhong@kaist.ac.kr)
- [Heedong Yang](https://github.com/heedong2y) (heedong@kaist.ac.kr)

### Contributor
- [Gihyuk Ko](https://github.com/gihyukko) (gihyuk.ko@kaist.ac.kr)

## Contributions
Contributions to the project are welcome! Use pull requests on Github.

## Publications
