import React from 'react';
import { Modal, Button, Container, Form } from 'react-bootstrap';
import './GenericModal.css';
import keycloak from "../Keycloak";

function ImportScannableModal({ handleClose, show, objectType, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate }) {

  function handleSubmit(event) {
    event.preventDefault();
    const jsonData = JSON.parse(event.target[0].value);

    if(keycloak.authenticated) {
      keycloak.updateToken(120).then(() => {
        localStorage.setItem('token', keycloak.token);
      })

      const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': String('Bearer ' + localStorage.getItem('token')) },
        body: JSON.stringify(jsonData)
      };
      
      fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's/import'), requestOptions)
        .then(response => {
          if (response.ok) {
            return response.json();
          }
          throw new Error('Something went wrong');
        })
        .then(data => {
          setSuccessMessage(String("Import of " + data + " " + (data === 1 ? ( "object") : ( "objects" )) + " succeeded."));
          setShowSuccess(true);
          handleUpdate();
        })
        .catch(() => {
          setErrorMessage("Import failed.");
          setShowError(true);
        });
    }

  };


  return (
    <Container>
      <Modal show={show}>
        <Modal.Header className="d-flex justify-content-center mb-3">
          <Modal.Title id="contained-modal-title-vcenter" className='fw-light text-secondary'>
            Import {objectType === "iprange" ? ( "IP ranges") : ( objectType + "s" )}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleSubmit} id="importForm">
            <Form.Group className='w-75 mx-auto'>
              <p className='text-secondary'>Enter {objectType === "iprange" ? ( "IP ranges") : ( objectType + "s" )} as JSON :</p>
              <Form.Control as="textarea" aria-label="objects-json" className="mb-4" />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer className="d-flex justify-content-center">
          <Button form='importForm' type="submit" className='btn-success rounded-pill me-3' onClick={handleClose}>Submit</Button>
          <Button className='btn-danger rounded-pill ms-3' onClick={handleClose}>Cancel</Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default ImportScannableModal;