import React from 'react';

export default class ErrorComponent extends React.Component {

    render() {
        return (
            <React.Fragment>
                <div className="position-relative overflow-hidden p-3 p-md-5 m-md-3 text-center">
                    <div className="col-md-5 p-lg-5 mx-auto my-5">
                        <h1 className="display-4 font-weight-normal">Ooops ... Smth went wrong</h1>
                    </div>
                </div>
            </React.Fragment>

        );
    }
}
