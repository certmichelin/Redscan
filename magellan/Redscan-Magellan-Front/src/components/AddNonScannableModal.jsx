import React from 'react';
import { Modal, Button, Container, Form, FloatingLabel } from 'react-bootstrap';
import './GenericModal.css';
import keycloak from "../Keycloak";

function AddNonScannableModal({ handleClose, show, objectType, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate, handleBlock }) {

  async function handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(event.target);

    const jsonData = {};

    if (objectType === "ip") {
      jsonData.value = data.get('name');
    }
    else {
      jsonData.name = data.get('name');
    }

    if(keycloak.authenticated) {
      keycloak.updateToken(120).then(() => {
        localStorage.setItem('token', keycloak.token);
      })

      const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': String('Bearer ' + localStorage.getItem('token')) },
        body: JSON.stringify(jsonData)
      };

      setTimeout(() => {
        fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's'), requestOptions)
          .then(response => {
            if (response.ok) {
              setSuccessMessage("Creation succeeded.");
              setShowSuccess(true);
              handleUpdate();
            }
            else {
              setErrorMessage("Creation failed.");
              setShowError(true);
            }
        })
        .then(() => {
          if (data.get("blocked")) {
            const jsonData = {
              "id": data.get('name'),
              "blocked": false
            };
            handleBlock(jsonData);
          }
        });
      }, 1000);
    }
  }
  
  function renderNameField() {
    if (objectType === "ip") {
      return  <FloatingLabel controlId="name" label="Value" className='text-secondary mb-3'>
                <Form.Control type="text" placeholder="Value" name="name" />
              </FloatingLabel>;
    }
    else {
      return  <FloatingLabel controlId="name" label="Name" className='text-secondary mb-3'>
                <Form.Control type="text" placeholder="Name" name="name" />
              </FloatingLabel>;
    }
  }
  
  return (
    <Container>
      <Modal show={show}>
        <Modal.Header className="d-flex justify-content-center mb-3">
          <Modal.Title id="contained-modal-title-vcenter" className='fw-light text-secondary'>
            Add {objectType === "ip" ? ("IP") : (objectType)}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleSubmit} id="addForm">
            <Form.Group className='w-50 mx-auto'>
              {renderNameField()}
              <Form.Check reverse type="switch" label="Blocked" className='text-secondary p-0 mb-3' name="blocked" />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer className="d-flex justify-content-center">
          <Button form='addForm' type="submit" className='btn-success rounded-pill me-3' onClick={handleClose}>Submit</Button>
          <Button className='btn-danger rounded-pill ms-3' onClick={handleClose}>Cancel</Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );

}

export default AddNonScannableModal;