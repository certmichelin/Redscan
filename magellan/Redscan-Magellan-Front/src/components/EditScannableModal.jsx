import React from 'react';
import { Modal, Button, Container, Form, Col, Row } from 'react-bootstrap';
import './GenericModal.css';
import keycloak from "../Keycloak";

function EditScannableModal({ handleClose, show, item, objectType, setSuccessMessage, setShowSuccess, setErrorMessage, setShowError, handleUpdate }) {

    function handleSubmit(event) {
      event.preventDefault();
      const data = new FormData(event.target);
      item.serviceLevel = data.get('serviceLevel');
  
      if (objectType === "masterdomain") {
        item.inScope = (data.get('inScope') === "on");
        item.reviewed = (data.get('reviewed') === "on");
      }
  
      if(keycloak.authenticated) {
        keycloak.updateToken(120).then(() => {
          localStorage.setItem('token', keycloak.token);
        })
  
        const requestOptions = {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'Authorization': String('Bearer ' + localStorage.getItem('token')) },
          body: JSON.stringify(item)
        };

        fetch(String('https://' + process.env.REACT_APP_PUBLIC_DOMAIN + '/magellan-api/api/' + objectType + 's'), requestOptions)
          .then(async response => {
            if (response.ok) {
              setSuccessMessage("Update succeeded.");
              setShowSuccess(true);
              handleUpdate();
            }
            else {
              setErrorMessage("Update failed.");
              setShowError(true);
            }
          });
      }
    };


    return (
      <Container>
        <Modal show={show}>
          <Modal.Header className="d-flex justify-content-center mb-3">
            <Modal.Title id="contained-modal-title-vcenter" className='fw-light text-secondary'>
              Edit {objectType === "iprange" ? ( "IP range") : ( objectType )} <b>{item != null ? objectType === "iprange" ? item.cidr : item.name : "undefined"}</b>
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form onSubmit={handleSubmit} id="editForm">
              <Form.Group className='w-50 mx-auto'>
                {objectType === "masterdomain" &&
                  <Form.Group>
                    <Form.Check reverse type="checkbox" label="In scope" className='text-secondary p-0 mb-3' name="inScope" defaultChecked={item != null ? item.inScope : "undefined"} />
                    <Form.Check reverse type="checkbox" label="Reviewed" className='text-secondary p-0 mb-3' name="reviewed" defaultChecked={item != null ? item.reviewed : "undefined"} />
                  </Form.Group>
                }
                <Row className='d-flex align-items-center  mb-3'>
                  <Col className='d-flex align-items-center justify-content-end pe-1'>
                    <p className='text-secondary text-end m-0'>Service level</p>
                  </Col>
                  <Col className='d-flex align-items-center justify-content-start ps-1'>
                    <Form.Select className='modal-select text-start w-auto text-secondary border-0 bg-light'  name="serviceLevel" defaultValue={item != null ? item.serviceLevel : "undefined"}>
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
            <Button form='editForm' type="submit" className='btn-success rounded-pill me-3' onClick={handleClose}>Apply changes</Button>
            <Button className='btn-danger rounded-pill ms-3' onClick={handleClose}>Cancel</Button>
          </Modal.Footer>
        </Modal>
      </Container>
    );
  }

  export default EditScannableModal;