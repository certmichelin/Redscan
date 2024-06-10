import React from 'react';
import { Modal, Button, Container, Form, Col, Row, FloatingLabel } from 'react-bootstrap';
import './GenericModal.css';
import keycloak from "../Keycloak";

function AddScannableModal({ handleClose, show, objectType, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate }) {
    
  function handleSubmit(event) {
    event.preventDefault();
    const data = new FormData(event.target);

    const jsonData = {
      "serviceLevel": data.get('serviceLevel'),
    };

    if (objectType === "iprange") {
      jsonData.cidr = data.get('name');
    }
    else {
      jsonData.name = data.get('name');
    }

    if (objectType === "masterdomain") {
      jsonData.inScope = (data.get('inScope') === "on");
      jsonData.reviewed = (data.get('reviewed') === "on");
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
        });
    }
  }

  function renderNameField() {
    if (objectType === "iprange") {
      return  <FloatingLabel controlId="name" label="CIDR" className='text-secondary mb-3'>
                <Form.Control type="text" placeholder="CIDR" name="name" />
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
            Add {objectType === "iprange" ? ( "IP range") : ( objectType )}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleSubmit} id="addForm">
            <Form.Group className='w-50 mx-auto'>
              {renderNameField()}
              {objectType === "masterdomain" &&
                <Form.Group>
                  <Form.Check reverse type="checkbox" label="In scope" className='text-secondary p-0 mb-3' name="inScope" />
                  <Form.Check reverse type="checkbox" label="Reviewed" className='text-secondary p-0 mb-3' name="reviewed" />
                </Form.Group>
              }
              <Row className='d-flex align-items-center  mb-3'>
                <Col className='d-flex align-items-center justify-content-end pe-1'>
                  <p className='text-secondary text-end m-0'>Service level</p>
                </Col>
                <Col className='d-flex align-items-center justify-content-start ps-1'>
                  <Form.Select className='modal-select text-start w-auto text-secondary border-0 bg-light' name="serviceLevel">
                    <option value="1">Gold</option>
                    <option value="2">Silver</option>
                    <option value="3">Bronze</option>
                  </Form.Select>
                </Col>
              </Row>
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

export default AddScannableModal;