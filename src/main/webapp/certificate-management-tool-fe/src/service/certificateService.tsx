import axios from "axios";
import ChallengeRequest from "../model/ChallengeRequest";
import ChallengeResponse from "../model/ChallengeResponse";
import CertificateResponse from "../model/CertificateResponse";
import CertificateModel from "../model/CertificateModel";

const host = 'http://localhost:8080/';

function mapResponseDataToArray(response: any) {
    return response.data
        .map((element: any) => new CertificateResponse(
            element.certificateBody,
            element.principleDomain,
            element.dateOfIssue,
            element.dateOfExpiration
            )
        )
}

export function submitChallenge(userName: string, parsedDomains: string[]) {
    const challengeRequest = new ChallengeRequest(userName, parsedDomains);
    return axios.post(host + 'challenge', challengeRequest)
        .then(response => {
            return response.data.map((element: any) => new ChallengeResponse(element.content, element.fileName, element.host))
        })
}

export function createCertificate(userName: string, unparsedDomains: string) {
    const url = host + 'certificate?userName=' + userName + '&domains=' + unparsedDomains;
    return axios.post(url).then(response => mapResponseDataToArray(response))
};

export function getCertificate(userName: string, certificateId: number): Promise<CertificateModel> {
    const url = host + 'certificate/information?userName=' + userName + '&certificateId=' + certificateId;
    return axios.get(url)
        .then(response => response.data
            .map((element:any) => new CertificateModel(
                element.certificateId,
                element.certificateBody,
                element.principleDomain,
                element.dateOfIssue,
                element.dateOfExpiration
            )))
}

export function getAllCertificatesForUserName(userName: string): Promise<Array<CertificateModel>> {
    const url = host + 'certificate?userName=' + userName;
    return axios.get(url)
        .then(response => response.data
            .map((element: any) => new CertificateModel(
                element.certificateId,
                element.certificateBody,
                element.principleDomain,
                element.dateOfIssue,
                element.dateOfExpiration
            )));
};