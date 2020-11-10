package gui;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable{

	private Seller entity;
	
	private SellerService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	@FXML
	private TextField txtId;
	
	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;
	
	@FXML
	private DatePicker dpBirthDate;
	
	@FXML
	private TextField txtBaseSalary;
	
	@FXML
	private Label labelErrorName;
	
	@FXML
	private Label labelErrorEmail;
	
	@FXML
	private Label labelErrorBirthDate;
	
	@FXML
	private Label labelErrorBaseSalary;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	public void setSeller(Seller entity) {
		this.entity = entity;
	}
	
	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	public void subscribeDataChangeListener(DataChangeListener listener) {
		this.dataChangeListeners.add(listener);
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if(this.entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if(this.service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			this.entity = getFormData();
			this.service.saveOrUpdate(this.entity);	
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		}
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}
		catch(DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	private void notifyDataChangeListeners() {
		for(DataChangeListener listener : this.dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();
		
		ValidationException exception = new ValidationException("Validation error");
		
		obj.setId(Utils.tryParseToInt(this.txtId.getText()));
		if(this.txtName.getText() == null || this.txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(this.txtName.getText());
		
		if(exception.getErrors().size() > 0) {
			throw exception;
		}
		
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {		
		this.initializeNodes();
	}
	
	private void initializeNodes() {
		Constraints.setTextFieldInteger(this.txtId);
		Constraints.setTextFieldMaxLength(this.txtName, 70);
		Constraints.setTextFieldDouble(this.txtBaseSalary);
		Constraints.setTextFieldMaxLength(this.txtEmail, 60);
		Utils.formatDatePicker(this.dpBirthDate, "dd/MM/yyyy");
	}
	
	public void updateFormData() {
		if(entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		this.txtId.setText(String.valueOf(this.entity.getId()));
		this.txtName.setText(this.entity.getName());
		this.txtEmail.setText(this.entity.getEmail());
		Locale.setDefault(Locale.US);
		this.txtBaseSalary.setText(String.format("%.2f", this.entity.getBaseSalary()));
		if(this.entity.getBirthDate() != null) {
			this.dpBirthDate.setValue(LocalDate.ofInstant(this.entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
			this.labelErrorName.setText(errors.get("name"));
		}
	}
	
}
